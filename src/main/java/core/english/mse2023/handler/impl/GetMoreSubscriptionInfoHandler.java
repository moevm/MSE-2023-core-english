package core.english.mse2023.handler.impl;

import core.english.mse2023.aop.annotation.handler.InlineButtonType;
import core.english.mse2023.component.InlineKeyboardMaker;
import core.english.mse2023.constant.InlineButtonCommand;
import core.english.mse2023.dto.InlineButtonDTO;
import core.english.mse2023.encoder.InlineButtonDTOEncoder;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.model.Lesson;
import core.english.mse2023.service.LessonService;
import core.english.mse2023.service.SubscriptionService;
import core.english.mse2023.util.builder.InlineKeyboardBuilder;
import core.english.mse2023.util.utilities.TelegramInlineButtonsUtils;
import core.english.mse2023.util.utilities.TelegramMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.UUID;

@Component
@InlineButtonType
@RequiredArgsConstructor
public class GetMoreSubscriptionInfoHandler implements Handler {

    private final LessonService lessonService;

    private final InlineKeyboardMaker inlineKeyboardMaker;

    @Override
    public List<BotApiMethod<?>> handle(Update update) {

        InlineButtonDTO buttonData = InlineButtonDTOEncoder.decode(update.getCallbackQuery().getData());

        List<Lesson> lessons = lessonService.getAllLessonsForSubscription(UUID.fromString(buttonData.getData()));

        EditMessageReplyMarkup editMessageReplyMarkup = TelegramMessageUtils.editMessageReplyMarkup(
                update.getCallbackQuery().getMessage().getChatId().toString(),
                update.getCallbackQuery().getMessage().getMessageId(),
                inlineKeyboardMaker.getSubscriptionLessonsMenu(lessons, buttonData.getData())
        );

        return List.of(editMessageReplyMarkup);
    }

    @Override
    public BotCommand getCommandObject() {
        return InlineButtonCommand.GET_MORE_SUBSCRIPTION_INFO;
    }
}
