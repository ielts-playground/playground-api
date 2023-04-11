package org.ielts.playground.controller;

import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.service.ExamService;
import org.springframework.validation.annotation.Validated;
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
        request.setExamPartId(id);
        this.service.submit(request);
    }
}
