package core.english.mse2023.hadler;

import core.english.mse2023.constant.Command;
import core.english.mse2023.hadler.interfaces.Handler;
import core.english.mse2023.model.User;
import core.english.mse2023.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllTeachersHandler implements Handler {

    private static final String START_TEXT = "Список зарегистрированных преподавателей:\n%s";
    private static final String NO_TEACHERS_TEXT = "Зарегистрированные преподаватели отсутствуют в системе.";
    private static final String USER_DATA_PATTERN = " - %s%s";

    private final UserService service;

    @Override
    public List<SendMessage> handle(Update update) {

        List<User> teachers = service.getAllTeachers();

        SendMessage sendMessage;

        if (teachers.isEmpty()) {
            sendMessage = createMessage(update.getMessage().getChatId().toString(), NO_TEACHERS_TEXT);
        } else {
            sendMessage = createMessage(update.getMessage().getChatId().toString(), String.format(START_TEXT, getTeachersDataText(teachers)));
        }

        return List.of(sendMessage);
    }

    private String getTeachersDataText(List<User> teachers) {
        StringBuilder stringBuilder = new StringBuilder();

        for (User teacher : teachers) {

            stringBuilder.append(
                    String.format(USER_DATA_PATTERN,
                            (teacher.getLastName() != null) ? (teacher.getLastName() + " ") : "", // Teacher's last name if present
                            teacher.getName() // Teacher's name (always present)
                    )
            );
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public String getCommand() {
        return Command.GET_ALL_TEACHERS;
    }

}
