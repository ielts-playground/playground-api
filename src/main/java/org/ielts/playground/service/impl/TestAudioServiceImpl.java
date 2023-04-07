package org.ielts.playground.service.impl;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.exception.InternalServerException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.model.response.TestAudioResponse;
import org.ielts.playground.repository.TestAudioRepository;
import org.ielts.playground.service.TestAudioService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class TestAudioServiceImpl implements TestAudioService {
    private final TestAudioRepository repository;

    public TestAudioServiceImpl(TestAudioRepository repository) {
        this.repository = repository;
    }

    @Override
    public TestAudioResponse get(Long testId) {
        try {
            return this.repository.findOneByTestId(testId)
                    .map(testAudio -> TestAudioResponse.builder()
                            .test(testId)
                            .name(testAudio.getName())
                            .type(MediaType.parseMediaType(testAudio.getType()))
                            .data(testAudio.getData())
                            .build())
                    .orElseThrow(() -> new NotFoundException(ValidationConstants.AUDIO_NOT_FOUND));
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerException(ex.getMessage(), ex);
        }
    }
}
