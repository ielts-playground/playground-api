package org.ielts.playground.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExamAnswerRetrievalResponse {
    private Long examId;
    private String skill;
    private Map<String, String> answers;
}
