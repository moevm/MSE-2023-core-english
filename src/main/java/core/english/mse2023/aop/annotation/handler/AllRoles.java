package core.english.mse2023.aop.annotation.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@AllRegisteredRoles
@GuestRole
@Retention(RetentionPolicy.RUNTIME)
public @interface AllRoles {
}
