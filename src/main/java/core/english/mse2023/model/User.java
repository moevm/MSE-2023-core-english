package core.english.mse2023.model;

import core.english.mse2023.model.dictionary.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
public class User extends BaseEntity {

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private String telegramId;

    @Column(name = "chat_id", nullable = false, unique = true)
    private String chatId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "password")
    private String password;

}
