package org.ielts.playground.service;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.ExamAnswerRetrievalResponse;
import org.ielts.playground.model.response.ResultAllExamIdResponse;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface ExamService {
    /**
     * Submits an exam with the examiner's answers.
     *
     * @param request the examiner's answers.
     */
    void submit(@NotNull ExamSubmissionRequest request);

    /**
     * Retrieves a writing test by a specific examination's id.
     *
     * @param examId the examination's id.
     */
    WritingTestRetrievalResponse retrieveWritingTest(@NotNull Long examId);

    /**
     * Retrieves an examination's answer sheet
     * by a specific examination's id.
     *
     * @param examId the examination's id.
     * @param partType the test skill.
     */
    ExamAnswerRetrievalResponse retrieveExamAnswer(
            @NotNull Long examId,
            @NotNull PartType partType);

    ResultAllExamIdResponse getAllExamNotGraded(Long page, Long size);

}
