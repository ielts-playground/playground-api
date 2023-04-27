package org.ielts.playground.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamFinalResultResponse {
    @JsonProperty("id")
    private Long examId;

    @JsonProperty("reading")
    private Long readingCorrectAnswers;

    @JsonProperty("listening")
    private Long listeningCorrectAnswers;

    @JsonProperty("writing")
    private Long writingEvaluation;

    private String examiner;
    private UserInfoResponse examinee;
}
