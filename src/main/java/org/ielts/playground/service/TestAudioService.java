package org.ielts.playground.service;

import org.ielts.playground.model.response.TestAudioResponse;

import javax.validation.constraints.NotNull;

public interface TestAudioService {
    /**
     * Retrieves an audio for a specific test.
     *
     * @param testId the test's id.
     * @throws org.ielts.playground.common.exception.NotFoundException when no audio is found.
     */
    @NotNull TestAudioResponse get(@NotNull Long testId);
}
