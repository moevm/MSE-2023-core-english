package core.english.mse2023.handler.impl;
import core.english.mse2023.aop.annotation.handler.AdminRole;
import core.english.mse2023.aop.annotation.handler.InlineButtonType;
import core.english.mse2023.aop.annotation.handler.TeacherRole;
import core.english.mse2023.component.ReplyKeyboardMaker;
import core.english.mse2023.constant.ButtonCommand;
import core.english.mse2023.constant.InlineButtonCommand;
import core.english.mse2023.dto.InlineButtonDTO;
import core.english.mse2023.encoder.InlineButtonDTOEncoder;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.model.Lesson;
import core.english.mse2023.model.dictionary.LessonStatus;
import core.english.mse2023.service.LessonService;
import core.english.mse2023.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@InlineButtonType
@TeacherRole
@AdminRole
public class CancelLessonHandler implements Handler {
    private static final String DONE_TEXT = "Выбранный урок отменён.";
    private static final String ENDED_TEXT = "Невозможно отменить урок. Он уже завершён.";
    private static final String IN_PROGRESS_TEXT = "Невозможно отменить урок. Он уже начат.";

    private final LessonService lessonService;

    @Override
    public List<BotApiMethod<?>> handle(Update update) {
        InlineButtonDTO buttonData = InlineButtonDTOEncoder.decode(update.getCallbackQuery().getData());

        UUID lessonId = UUID.fromString(buttonData.getData());
        LessonStatus status = lessonService.cancelLesson(lessonId);
        SendMessage message = switch (status) {
            case ENDED -> SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                    .text(ENDED_TEXT)
                    .build();
            case IN_PROGRESS -> SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                    .text(IN_PROGRESS_TEXT)
                    .build();
            default -> SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                    .text(DONE_TEXT)
                    .build();
        };

        return List.of(message);
    }

    @Override
    public BotCommand getCommandObject() {
        return InlineButtonCommand.CANCEL_LESSON;
    }


}