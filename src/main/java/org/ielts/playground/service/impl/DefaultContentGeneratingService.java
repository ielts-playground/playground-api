package org.ielts.playground.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import org.ielts.playground.common.constant.ContentGeneratingConstants;
import org.ielts.playground.service.ContentGeneratingService;

@Service(ContentGeneratingConstants.DEFAULT_GENERATOR)
public class DefaultContentGeneratingService implements ContentGeneratingService {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
