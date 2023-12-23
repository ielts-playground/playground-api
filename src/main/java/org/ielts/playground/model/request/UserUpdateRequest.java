package org.ielts.playground.model.request;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

@Data
public class UserUpdateRequest {
    @NotNull
    private String username;

    @Nullable
    private String password;

    @Nullable
    private String email;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String phoneNumber;

    @Nullable
    private String subscription;

    @Nullable
    private Boolean activated;
}
