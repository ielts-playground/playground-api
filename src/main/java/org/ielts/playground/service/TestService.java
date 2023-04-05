package org.ielts.playground.service;

import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.TestCreationResponse;

import javax.validation.constraints.NotNull;

public interface TestService {
    /**
     * Creates or updates a test in a specific part's information.
     *
     * @param request the part's information.
     */
    TestCreationResponse create(@NotNull TestCreationRequest request);
}
