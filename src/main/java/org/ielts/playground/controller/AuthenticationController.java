package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.RequireClient;
import org.ielts.playground.common.constant.PrivateClientConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.annotation.PermitAll;
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

    @PermitAll
    @PostMapping(PathConstants.API_AUTHENTICATION_URL)
    public JwtResponse authenticate(
            @Validated @RequestBody AuthenticationRequest request) {
        return this.userService.exists(request)
                .map(userDetails -> {
                    String token = this.jwtUtils.generateToken(userDetails);
                    return JwtResponse.builder().token(token).build();
                })
                .orElseThrow(UnauthorizedRequestException::new);
    }

    @RequireAdmin
    @PostMapping(PathConstants.API_REGISTRATION_URL)
    public void register(
            @Validated @RequestBody UserRegistrationRequest userRegistrationRequest) {
        this.userService.createUser(userRegistrationRequest);
    }

    @RequireClient(name = PrivateClientConstants.V2)
    @PostMapping(PathConstants.PRIVATE_REGISTRATION_URL)
    public void registerPrivately(
            @Validated @RequestBody UserRegistrationRequest userRegistrationRequest) {
        this.userService.createUser(userRegistrationRequest);
    }
}
