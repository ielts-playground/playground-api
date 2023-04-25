package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;
import org.ielts.playground.service.ExamService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExamController {
    private final ExamService service;

    public ExamController(ExamService service) {
        this.service = service;
    }

    @PutMapping(PathConstants.API_EXAM_SUBMISSION_URL)
    public void submit(
            @PathVariable Long id,
            @Validated @RequestBody ExamSubmissionRequest request) {
        request.setExamTestId(id);
        this.service.submit(request);
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_EXAM_WRITING_TEST_RETRIEVAL_URL)
    public WritingTestRetrievalResponse retrieveWritingTest(
        @PathVariable Long id) {
        return this.service.retrieveWritingTest(id);
    }
}
