package org.ielts.playground.service;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.DisplayAllDataResponse;
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
    DisplayAllDataResponse retrieveRandomExamBySkill(@Nullable Long examId, PartType skill);

}
