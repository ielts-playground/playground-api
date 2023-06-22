package org.ielts.playground.controller;

import org.ielts.playground.common.annotation.RequireAdmin;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.ExamAnswerRetrievalResponse;
import org.ielts.playground.model.response.ExamFinalResultResponse;
import org.ielts.playground.model.response.ResultAllExamIdResponse;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;
import org.ielts.playground.service.ExamService;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @RequireAdmin
    @GetMapping(PathConstants.API_EXAM_ANSWER_RETRIEVAL_URL)
    public ExamAnswerRetrievalResponse retrieveExamAnswers(
            @PathVariable Long id,
            @PathVariable String skill) {
        return this.service.retrieveExamAnswer(id, PartType.of(skill));
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_EXAM_FINAL_RESULT_URL)
    public ExamFinalResultResponse retrieveFinalResult(
            @PathVariable Long id) {
        return this.service.retrieveFinalResult(id);
    }

    @RequireAdmin
    @GetMapping(PathConstants.API_GET_EXAM_NOT_GRADED_URL)
    public ResultAllExamIdResponse search(@RequestParam Long page, @RequestParam Long size) {
        return this.service.getAllExamNotGraded(page, size);
    }
}
