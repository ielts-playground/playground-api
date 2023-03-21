package org.ielts.playground.controller;

import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ielts.playground.common.annotation.AdminPermitted;
import org.ielts.playground.common.annotation.Permitted;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.exception.UnauthorizedRequestException;
import org.ielts.playground.model.request.AuthenticationRequest;
import org.ielts.playground.model.request.UserRegistrationRequest;
import org.ielts.playground.model.response.JwtResponse;
import org.ielts.playground.service.UserService;
import org.ielts.playground.utils.JwtUtils;

@RestController
public class AuthenticationController {
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthenticationController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Permitted
    @PostMapping(PathConstants.API_AUTHENTICATION_URL)
    public JwtResponse authenticate(
            @RequestBody @NotNull AuthenticationRequest request) {
        return this.userService.exists(request)
                .map(userDetails -> {
                    String token = this.jwtUtils.generateToken(userDetails);
                    return JwtResponse.builder().token(token).build();
                })
                .orElseThrow(UnauthorizedRequestException::new);
    }

    @AdminPermitted
    @PostMapping(PathConstants.API_REGISTRATION_URL)
    public void register(
            @RequestBody @NotNull UserRegistrationRequest userRegistrationRequest) {
        this.userService.createUser(userRegistrationRequest);
    }
}
