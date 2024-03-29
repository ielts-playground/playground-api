package org.ielts.playground.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmissionRequest {
    @JsonAlias("submitId")
    private Long examTestId;

    @NotNull
    private String skill;

    @NotNull
    private Map<String, String> answers;
}
