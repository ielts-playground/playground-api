package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.annotation.RequireClient;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.constant.PrivateClientConstants;
import org.ielts.playground.common.constant.RequestConstants;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.common.enumeration.Subscription;
import org.ielts.playground.model.dto.PointDTO;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.DisplayAllDataResponse;
import org.ielts.playground.model.response.TestCreationResponse;
import org.ielts.playground.service.TestService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
public class TestController {
    private final TestService service;

    public TestController(TestService service) {
        this.service = service;
    }

    @RequireAdmin
    @PutMapping(PathConstants.API_TEST_CREATION_URL)
    public TestCreationResponse create(
            @RequestPart(name = RequestConstants.AUDIO, required = false) final MultipartFile audio,
            @Validated @RequestPart(RequestConstants.CONTENT) final TestCreationRequest content) {
        content.setAudio(audio);
        return this.service.create(content);
    }

    @RequireAdmin
    @PutMapping(PathConstants.API_TEST_ALL_CREATION_URL)
    public TestCreationResponse createAll(
            @RequestPart(name = RequestConstants.AUDIO, required = false) final MultipartFile audio,
            @RequestPart(name = RequestConstants.SUBSCRIPTION, required = false) final String subscription,
            @Validated @RequestPart(RequestConstants.LISTENING) final TestCreationRequest listening,
            @Validated @RequestPart(RequestConstants.READING) final TestCreationRequest reading,
            @Validated @RequestPart(RequestConstants.WRITING) final TestCreationRequest writing) {
        listening.setAudio(audio);
        Optional.ofNullable(Subscription.of(subscription)).ifPresent(s -> {
            listening.setSubscription(s);
            reading.setSubscription(s);
            writing.setSubscription(s);
        });
        return this.service.createAll(
                listening,
                reading,
                writing
        );
    }

    @GetMapping(PathConstants.API_GET_TEST_READING_SKILL)
    public DisplayAllDataResponse retrieveRandomReadingExam(
            @Nullable @RequestParam(name = "id", required = false) Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.READING);
    }

    @GetMapping(PathConstants.API_GET_TEST_LISTENING_SKILL)
    public DisplayAllDataResponse retrieveRandomListeningExam(
            @Nullable @RequestParam(name = "id", required = false) Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.LISTENING);
    }

    @GetMapping(PathConstants.API_GET_TEST_WRITING_SKILL)
    public DisplayAllDataResponse retrieveRandomWritingExam(
            @Nullable @RequestParam(name = "id", required = false) Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.WRITING);
    }

    @RequireClient(name = PrivateClientConstants.V2)
    @GetMapping(PathConstants.PRIVATE_RANDOM_TEST_RETRIEVAL_URL)
    public DisplayAllDataResponse retrieveRandomExamPrivately(
            @RequestParam(name = "skill") String skill,
            @Nullable @RequestParam(name = "id", required = false) Long examId) {
        return this.service.retrieveRandomExamBySkill(examId, PartType.of(skill));
    }

    @PostMapping(PathConstants.API_EVALUATION_WRITING)
    public void savePointWriting(@PathVariable Long examId, @RequestBody PointDTO pointDTO){
         this.service.savePointWritingByExamId(examId, pointDTO);
    }

}
