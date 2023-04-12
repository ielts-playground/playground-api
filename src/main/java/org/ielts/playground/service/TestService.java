package org.ielts.playground.service;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.TestCreationResponse;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

public interface TestService {
    /**
     * Creates or updates a test in a specific part's information.
     *
     * @param request the part's information.
     */
    TestCreationResponse create(@NotNull TestCreationRequest request);

    /**
     * Joins an exam with a specific skill. It will create new exam
     * if the {@code examId} is not specified.
     *
     * @param examId the exam's id.
     * @param partType the parts' skill.
     */
    void join(@Nullable Long examId, @NotNull PartType partType);
}
