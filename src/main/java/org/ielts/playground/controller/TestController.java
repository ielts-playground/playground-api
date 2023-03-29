package org.ielts.playground.controller;

import lombok.extern.log4j.Log4j2;
import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.utils.SecurityUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Log4j2
@RestController
@Profile({ "dev", "default" })
public class TestController {
    private final SecurityUtils securityUtils;

    public TestController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @PostConstruct
    public void warn() {
        log.warn("You're in the development environment, all testing endpoints are now accessible.");
    }

    @GetMapping(PathConstants.PUBLIC_TEST_URL)
    public String publicTest() {
        return "Hi!";
    }

    @GetMapping(PathConstants.PRIVATE_TEST_URL)
    public String privateTest() {
        return "Welcome to my village!";
    }

    @GetMapping(PathConstants.API_TEST_URL)
    public String apiTest() {
        return String.format("I love you, %s!", this.securityUtils.getLoggedUsername());
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_ADMIN_TEST_URL)
    public String adminTest() {
        return String.format("Welcome back! Mr. %s!", this.securityUtils.getLoggedUsername());
    }
}
