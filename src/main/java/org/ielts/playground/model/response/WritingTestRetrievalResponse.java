package org.ielts.playground.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ielts.playground.model.entity.Component;

import java.util.List;

@Getter
@Setter
public class WritingTestRetrievalResponse {
    private Long examId;
    private Long examTestId;
    private List<ComponentWithPartNumber> components;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentWithPartNumber extends Component {
        private Long partNumber;
    }
}
