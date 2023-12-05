package org.ielts.playground.service;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.dto.PointDTO;
import org.ielts.playground.model.request.TestCreationRequest;
import org.ielts.playground.model.response.DisplayAllDataResponse;
import org.ielts.playground.model.response.ResultCheckingResponse;
import org.ielts.playground.model.response.TestCreationResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface TestService {
    /**
     * Creates or updates a test in a specific part's information.
     *
     * @param request the part's information.
     */
    TestCreationResponse create(@NotNull TestCreationRequest request);

    /**
     * Same as {@link #create(TestCreationRequest)} but supports 3 skills at once.
     */
    TestCreationResponse createAll(
            @NotNull TestCreationRequest listening,
            @NotNull TestCreationRequest reading,
            @NotNull TestCreationRequest writing
    );
    DisplayAllDataResponse retrieveRandomExamBySkill(@Nullable Long examId, PartType skill);
    ResultCheckingResponse checkReadingAndListeningResult(@NotNull Long examId);

    void savePointWritingByExamId(Long examId, PointDTO pointDTO);
}
