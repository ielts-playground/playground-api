package org.ielts.playground.controller;

import lombok.extern.log4j.Log4j2;
import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.annotation.RequireClient;
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

    /**
     * Retrieves the current logged user's name.
     */
    private String loggedUsername() {
        return this.securityUtils.getLoggedUsername();
    }

    public DevController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @PostConstruct
    public void warn() {
        log.warn("You're in the development environment, all testing endpoints are now accessible.");
    }

    @GetMapping(PathConstants.PUBLIC_DEV_URL)
    public String publicTest() {
        return "Hello World!";
    }

    @GetMapping(PathConstants.PRIVATE_DEV_URL)
    public String privateTest() {
        return String.format("Welcome to my village, %s!", this.loggedUsername());
    }

    @GetMapping(PathConstants.API_DEV_URL)
    public String apiTest() {
        return String.format("Hi, %s!", this.loggedUsername());
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_ADMIN_DEV_URL)
    public String adminTest() {
        return String.format("It's %s now. Don't forget your mission, %s!", LocalDateTime.now(), this.loggedUsername());
    }

    @RequireClient(name = "dev")
    @GetMapping(PathConstants.PRIVATE_CLIENT_DEV_URL)
    public String privateClientTest() {
        return String.format("Oh! It's you, %s.", this.loggedUsername());
    }
}
