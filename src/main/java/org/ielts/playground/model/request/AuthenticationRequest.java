package org.ielts.playground.model.request;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotNull
    private String username;

    @NotNull
    private String password;
}
