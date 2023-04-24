package org.ielts.playground.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayAllDataResponse {
    @JsonProperty("id")
    private Long examId;
    @JsonProperty("submitId")
    private Long examTestId;
    @JsonProperty("resourceId")
    private Long testId;
    private Map<Long, DisplayQuestionDataResponse> displayQuestionDataResponse;
    private Map<String, List<String>> listTypeQuestion;

}
