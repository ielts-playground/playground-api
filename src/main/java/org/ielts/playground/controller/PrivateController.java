package org.ielts.playground.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.ielts.playground.common.constant.PathConstants;

@RestController
public class PrivateController {
    @RequestMapping(PathConstants.PRIVATE_TEST_URL)
    public String test() {
        return "OK";
    }
}
