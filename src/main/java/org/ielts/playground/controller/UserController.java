package org.ielts.playground.controller;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.ielts.playground.common.annotation.RequireClient;
import org.ielts.playground.common.constant.PrivateClientConstants;
import org.ielts.playground.model.request.UserUpdateRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.exception.UnauthorizedRequestException;
import org.ielts.playground.model.response.UserInfoResponse;
import org.ielts.playground.service.UserService;
import org.ielts.playground.utils.SecurityUtils;

@RestController
public class UserController {

    private final SecurityUtils securityUtils;
    private final UserService userService;

    public UserController(
            UserService userService,
            SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping(PathConstants.API_USERS_INFO_URL)
    public UserInfoResponse info() {
        return Optional.ofNullable(this.securityUtils.getLoggedUsername())
                .map(this::getUserInfo)
                .orElseThrow(UnauthorizedRequestException::new);
    }

    @GetMapping(PathConstants.API_ADMIN_USERS_INFO_URL)
    public UserInfoResponse info(@PathVariable @NotNull String username) {
        return this.getUserInfo(username);
    }

    @RequireClient(name = PrivateClientConstants.V2)
    @PatchMapping(PathConstants.PRIVATE_USER_UPDATE_URL)
    public UserInfoResponse updatePrivately(
            @Validated @RequestBody UserUpdateRequest userUpdateRequest) {
        return this.userService.updateUserInfo(userUpdateRequest);
    }

    private UserInfoResponse getUserInfo(String username) {
        return this.userService.getUserInfo(username)
                .orElseThrow(UnauthorizedRequestException::new);
    }
}
