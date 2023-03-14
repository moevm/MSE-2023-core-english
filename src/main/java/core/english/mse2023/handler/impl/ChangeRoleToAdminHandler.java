package core.english.mse2023.handler.impl;

import core.english.mse2023.constant.ButtonCommand;
import core.english.mse2023.constant.Command;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.model.dictionary.UserRole;
import core.english.mse2023.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChangeRoleToAdminHandler implements Handler {

    private static final String SUCCESS_TEXT = "Ваша роль изменена на: %s";
    private static final String FAIL_TEXT = "Невозможно сменить роль на такую же, как у вас.";

    private final UserService service;

    @Override
    public List<SendMessage> handle(Update update) {

        boolean result = service.changeUserRole(update, UserRole.ADMIN);

        SendMessage message;

        if (!result) {
            message = createMessage(update.getMessage().getChatId().toString(), FAIL_TEXT, null);
        } else {
            message = createMessage(update.getMessage().getChatId().toString(), String.format(SUCCESS_TEXT, UserRole.ADMIN), null);
        }

        return List.of(message);
    }

    @Override
    public BotCommand getCommand() {
        return ButtonCommand.CHANGE_ROLE_TO_ADMIN;
    }
}
