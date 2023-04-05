package org.ielts.playground.controller;

import lombok.extern.log4j.Log4j2;
import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.utils.SecurityUtils;
import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Log4j2
@RestController
@Profile({ "dev", "default" })
public class DevController {
    private final SecurityUtils securityUtils;

    public DevController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @PostConstruct
    public void warn() {
        log.warn("You're in the development environment, all testing endpoints are now accessible.");
    }

    @GetMapping(PathConstants.PUBLIC_DEV_URL)
    public String publicTest() {
        return "Hi!";
    }

    @GetMapping(PathConstants.PRIVATE_DEV_URL)
    public String privateTest() {
        return "Welcome to my village!";
    }

    @GetMapping(PathConstants.API_DEV_URL)
    public String apiTest() {
        return String.format("I love you, %s!", this.securityUtils.getLoggedUsername());
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_ADMIN_DEV_URL)
    public String adminTest() {
        return String.format("Welcome back, Mr. %s! It's %s.", this.securityUtils.getLoggedUsername(), LocalDateTime.now());
    }
}
