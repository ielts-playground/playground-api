package org.ielts.playground.service;

import org.ielts.playground.model.request.ExamSubmissionRequest;

import javax.validation.constraints.NotNull;

public interface ExamService {
    /**
     * Submits an exam with the examiner's answers.
     *
     * @param request the examiner's answers.
     */
    void submit(@NotNull ExamSubmissionRequest request);
}
