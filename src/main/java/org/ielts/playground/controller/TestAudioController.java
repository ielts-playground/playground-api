package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.PermitAll;
import org.ielts.playground.common.annotation.RequireClient;
import org.ielts.playground.common.constant.CachingConstants;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.constant.PrivateClientConstants;
import org.ielts.playground.common.constant.RequestConstants;
import org.ielts.playground.model.response.TestAudioResponse;
import org.ielts.playground.service.TestAudioService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAudioController {
    private final TestAudioService service;

    public TestAudioController(TestAudioService service) {
        this.service = service;
    }

    // @Cacheable(key = "#testId", cacheNames = CachingConstants.TEST_AUDIO_GET_CACHE_NAME)
    @PermitAll
    @GetMapping(PathConstants.API_TEST_AUDIO_URL)
    public ResponseEntity<byte[]> audio(
            @PathVariable(RequestConstants.ID) Long testId) {
        final TestAudioResponse response = this.service.get(testId);
        return ResponseEntity.ok()
                .contentType(response.getType())
                .body(response.getData());
    }

    @RequireClient(name = PrivateClientConstants.V2)
    @GetMapping(PathConstants.PRIVATE_API_TEST_AUDIO_URL)
    public ResponseEntity<byte[]> privateAudio(
            @PathVariable(RequestConstants.ID) Long testId) {
        final TestAudioResponse response = this.service.get(testId);
        return ResponseEntity.ok()
                .contentType(response.getType())
                .body(response.getData());
    }
}
