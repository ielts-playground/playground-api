package org.ielts.playground.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResultCheckingResponse {
    private Long examId;
    private Map<String, Correctness> result;

    @Getter
    @Setter
    public static class Correctness {
        private long total;
        private long correct;
    }
}
