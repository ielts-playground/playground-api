package org.ielts.playground.model.request;

import javax.validation.constraints.NotNull;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class UserRegistrationRequest {
    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String email;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String phoneNumber;

    @Nullable
    private String subscription;

    @Nullable
    private Boolean activated;
}
