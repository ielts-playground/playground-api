package org.ielts.playground.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayAllDataResponse {

    private Map<Long, DisplayQuestionDataResponse> displayQuestionDataResponse;
    private Map<String, Set<String>> listTypeQuestion;

}
