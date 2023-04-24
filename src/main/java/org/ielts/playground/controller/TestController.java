package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.constant.RequestConstants;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.DisplayAllDataResponse;
import org.ielts.playground.model.response.TestCreationResponse;
import org.ielts.playground.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.annotation.Nullable;

import java.util.Map;

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

    @GetMapping(PathConstants.API_GET_TEST_READING_SKILL)
    public DisplayAllDataResponse retrieveRandomReadingExam(
            @Nullable @RequestParam(name = "id", required = false) Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.READING);
    }

    @GetMapping(PathConstants.API_GET_TEST_LISTENING_SKILL)
    public DisplayAllDataResponse retrieveRandomListeningExam(
            @Nullable @RequestParam(name = "id", required = false)Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.LISTENING);
    }

    @GetMapping(PathConstants.API_GET_TEST_WRITING_SKILL)
    public DisplayAllDataResponse retrieveRandomWritingExam(
            @Nullable @RequestParam(name = "id", required = false)Long examId){
        return this.service.retrieveRandomExamBySkill(examId, PartType.WRITING);
    }

    @GetMapping(PathConstants.API_CHECK_ANSWER)
    public Map<String, Long> checkAnswer(@RequestParam(name = "examId")Long examId){
        return this.service.checkAnswer(examId);
    }


}
