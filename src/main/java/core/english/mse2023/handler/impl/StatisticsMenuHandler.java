package core.english.mse2023.handler.impl;

import core.english.mse2023.aop.annotation.handler.TextCommandType;
import core.english.mse2023.component.ReplyKeyboardMaker;
import core.english.mse2023.constant.ButtonCommand;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.util.factory.TelegramMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

@Component
@TextCommandType
@RequiredArgsConstructor
public class StatisticsMenuHandler implements Handler {

    private static final String MESSAGE = "Вы перешли в раздел СТАТИСТИКА";
    private final ReplyKeyboardMaker replyKeyboardMaker;

    @Override
    public List<BotApiMethod<?>> handle(Update update) {

        return List.of(TelegramMessageUtils.createMessage(update.getMessage().getChatId().toString(),
                MESSAGE, replyKeyboardMaker.getStatisticsKeyboard()));
    }

    @Override
    public BotCommand getCommandObject() {
        return ButtonCommand.STATISTICS;
    }
}
