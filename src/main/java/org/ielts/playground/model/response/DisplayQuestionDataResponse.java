package org.ielts.playground.model.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayQuestionDataResponse {

    private List<ComponentDataResponse> leftContent;
    private List<ComponentDataResponse> rightContent;

}
