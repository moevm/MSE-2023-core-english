package core.english.mse2023.handler.impl;

import core.english.mse2023.aop.annotation.handler.AllRoles;
import core.english.mse2023.aop.annotation.handler.InlineButtonType;
import core.english.mse2023.constant.InlineButtonCommand;
import core.english.mse2023.dto.InlineButtonDTO;
import core.english.mse2023.encoder.InlineButtonDTOEncoder;
import core.english.mse2023.handler.Handler;
import core.english.mse2023.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;
import java.util.UUID;

@Component
@AllRoles
@InlineButtonType
@RequiredArgsConstructor
public class CancelSubscriptionHandler implements Handler {

    private final static String DONE_TEXT = "Подписка отменена";
    private final static String ALREADY_CANCELED_TEXT = "Невозможно отменить подписку. Она ранее уже была отменена!";

    private final SubscriptionService subscriptionService;

    @Override
    public List<BotApiMethod<?>> handle(Update update) {

        InlineButtonDTO buttonData = InlineButtonDTOEncoder.decode(update.getCallbackQuery().getData());

        UUID subscriptionId = UUID.fromString(buttonData.getData());

        boolean success = subscriptionService.cancelSubscription(subscriptionId);

        SendMessage message;

        if (!success) {
            message = SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                    .text(ALREADY_CANCELED_TEXT)
                    .build();
        } else {
            message = SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                    .text(DONE_TEXT)
                    .build();
        }

        return List.of(message);
    }

    @Override
    public BotCommand getCommandObject() {
        return InlineButtonCommand.CANCEL_SUBSCRIPTION;
    }
}
