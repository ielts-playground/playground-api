package org.ielts.playground.service;

import org.ielts.playground.model.request.ExamSubmissionRequest;
import org.ielts.playground.model.response.WritingTestRetrievalResponse;

import javax.validation.constraints.NotNull;

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
}
