package core.english.mse2023.tgbot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import core.english.mse2023.aop.annotation.handler.InlineButtonHandler;
import core.english.mse2023.cache.CacheData;
import core.english.mse2023.config.BotConfig;
import core.english.mse2023.dto.InlineButtonDTO;
import core.english.mse2023.encoder.InlineButtonDTOEncoder;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.handler.InteractiveHandler;
import core.english.mse2023.resolver.Resolver;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;

    private final Map<String, Handler> inlineButtonHandlers;

    // Cache for storing last used commands with require users input
    private final Cache<String, CacheData> cache = createCache();

    private final Resolver resolver;

    public TelegramBot(BotConfig config, Resolver resolver, @InlineButtonHandler List<Handler> inlineButtonHandlers) {
        this.config = config;
        this.resolver = resolver;
        this.inlineButtonHandlers = inlineButtonHandlers
                .stream()
                .collect(Collectors.toMap((handler -> handler.getCommandObject().getCommand()), Function.identity()));
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // If we received a message

            String command = update.getMessage().getText();

            Handler handler = resolver.getHandler(command);

            if (handler != null) {

                if (handler instanceof InteractiveHandler interactiveHandler) {
                    // If it needs user input

                    // This sends the initial message and initialise the handler for new user
                    executeBotApiMethods(interactiveHandler.handle(update));

                    // Creating cache data object with chosen handler
                    CacheData data = new CacheData(interactiveHandler);

                    // Adding newly created data object to cache
                    cache.put(update.getMessage().getFrom().getId().toString(), data);
                } else {
                    // If chosen handler doesn't need user input

                    executeBotApiMethods(handler.handle(update));
                }

            } else {
                // If the message is not a command

                // Checking if any command is in progress for this user
                CacheData commandData = cache.getIfPresent(update.getMessage().getFrom().getId().toString());

                // If command found - proceed the command
                if (commandData != null) {
                    executeBotApiMethods(commandData.updateData(update));
                }

                // By this if the command finished it work it can be deleted from cache
                triggerTimeBasedEvictionChecker(update.getMessage().getFrom().getId().toString());

            }

        } else if (update.hasCallbackQuery()) {
            // If we received an inline button press

            CacheData cacheData = cache.getIfPresent(update.getCallbackQuery().getFrom().getId().toString());

            // Checking if user who pressed the button has any ongoing processes
            if (cacheData != null) {
                InlineButtonDTO inlineButtonDTO = InlineButtonDTOEncoder.decode(update.getCallbackQuery().getData());

                // Checking if data from the button corresponds with the expected data
                if (cacheData.getHandler().getCommandObject().getCommand().equals(inlineButtonDTO.getCommand()) &&
                        (cacheData.getState().getStateIndex() - 1) == inlineButtonDTO.getStateIndex()) {

                    // If everything is ok - proceed the command
                    executeBotApiMethods(cacheData.updateData(update));

                    // By this if the command finished it work it can be deleted from cache
                    triggerTimeBasedEvictionChecker(update.getCallbackQuery().getFrom().getId().toString());
                }
            } else {
                InlineButtonDTO buttonData = InlineButtonDTOEncoder.decode(update.getCallbackQuery().getData());

                Handler handler = inlineButtonHandlers.get(buttonData.getCommand());

                if (handler != null) {
                    executeBotApiMethods(handler.handle(update));
                }
            }
        }
    }

    /**
     * Sends all the messages from the list
     *
     * @param methods - list of bot api methods to execute
     */
    public void executeBotApiMethods(List<? extends BotApiMethod<?>> methods) {
        for (BotApiMethod<?> method : methods) {
            try {
                execute(method);
                log.info("Reply sent");
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * This function gets cache's element to trigger time based eviction checker
     *
     * @param id - id of needed item from the cache
     */
    private void triggerTimeBasedEvictionChecker(String id) {
        cache.getIfPresent(id);
        cache.cleanUp();
    }

    private Cache<String, CacheData> createCache() {
        return Caffeine.newBuilder()
                .expireAfter(createTimeExpireRule())
                .removalListener(((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        if (value != null) {
                            value.getHandler().removeFromCacheBy(key);
                        }
                    }
                }))
                .build();
    }

    private Expiry<String, CacheData> createTimeExpireRule() {
        return new Expiry<>() {

            // Expire time in nanoseconds
            //                      min   sec   ms      ns
            final long expireTime = 20L * 60 * 1000 * 1000000;

            @Override
            public long expireAfterCreate(@Nonnull String s, @Nonnull CacheData cacheData, long l) {
                return Long.MAX_VALUE;
            }

            @Override
            public long expireAfterUpdate(@Nonnull String s, @Nonnull CacheData cacheData, long l, long l1) {
                return Long.MAX_VALUE;
            }

            @Override
            public long expireAfterRead(@Nonnull String s, @Nonnull CacheData cacheData, long l, long l1) {

                if (!cacheData.getState().hasNext()) {
                    return 10;
                }
                return expireTime;
            }
        };
    }
}
