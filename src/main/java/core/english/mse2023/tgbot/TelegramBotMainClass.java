package core.english.mse2023.tgbot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import core.english.mse2023.cache.CacheData;
import core.english.mse2023.config.BotConfig;
import core.english.mse2023.dto.InlineBtnDTO;
import core.english.mse2023.hadler.Handler;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBotMainClass extends TelegramLongPollingBot {
    private final BotConfig config;

    private final Map<String, Handler> handlers;

    // Cache for storing last used commands with require users input
    private final Cache<String, CacheData> cache = Caffeine.newBuilder()
            // Setting rule for checking if Command was finished or lifetime ran out
            .expireAfter(new Expiry<String, CacheData>() {

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
            })
            // Making sure handler's inner cache clears users data when command finishes/deletes
            .removalListener(((key, value, cause) -> {
                if (cause == RemovalCause.EXPIRED) {
                    if (value != null) {
                        value.getHandler().cleanUp(key);
                    }
                }
            }))
            .build();

    public TelegramBotMainClass(BotConfig config, List<Handler> handlers) {
        this.config = config;
        this.handlers = handlers
                .stream()
                .collect(Collectors.toMap(Handler::getCommand, Function.identity()));
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

            Handler handler = handlers.get(command);

            if (handler != null) {

                if (!handler.needsUserInteraction()) {
                    // If chosen handler doesn't need user input

                    sendMessages(handler.handle(update));
                } else {
                    // If it needs user input

                    // This sends the initial message and initialise the handler for new user
                    sendMessages(handler.handle(update));

                    // Creating cache data object with chosen handler
                    CacheData data = new CacheData(handler);

                    // Adding newly created data object to cache
                    cache.put(update.getMessage().getFrom().getId().toString(), data);
                }
            } else {
                // If the message is not a command

                // Checking if any command is in progress for this user
                CacheData commandData = cache.getIfPresent(update.getMessage().getFrom().getId().toString());

                // If command found - proceed the command
                if (commandData != null) {
                    sendMessages(commandData.updateData(update));
                }

                // By this if the command finished it work it can be deleted from cache
                triggerTimeBasedEvictionChecker(update.getMessage().getFrom().getId().toString());

            }

        } else if (update.hasCallbackQuery()) {
            // If we received an inline button press

            CacheData cacheData = cache.getIfPresent(update.getCallbackQuery().getFrom().getId().toString());

            // Checking if user who pressed the button has any ongoing processes
            if (cacheData != null) {
                InlineBtnDTO inlineBtnDTO = InlineBtnDTO.createFromString(update.getCallbackQuery().getData());

                // Checking if data from the button corresponds with the expected data
                if (cacheData.getHandler().getCommand().equals(inlineBtnDTO.getCommand()) &&
                        (cacheData.getState().getStateIndex() - 1) == inlineBtnDTO.getStateIndex()) {

                    // If everything is ok - proceed the command
                    sendMessages(cacheData.updateData(update));

                    // By this if the command finished it work it can be deleted from cache
                    triggerTimeBasedEvictionChecker(update.getCallbackQuery().getFrom().getId().toString());
                }
            }
        }
    }

    /**
     * Sends all the messages from the list
     * @param messages - list of messages to send
     */
    public void sendMessages(List<SendMessage> messages) {
        for (SendMessage message : messages) {
            try {
                execute(message);
                log.info("Reply sent");
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * This function gets cache's element to trigger time based eviction checker
     * @param id - id of needed item from the cache
     */
    private void triggerTimeBasedEvictionChecker(String id) {
        cache.getIfPresent(id);
        cache.cleanUp();
    }
}