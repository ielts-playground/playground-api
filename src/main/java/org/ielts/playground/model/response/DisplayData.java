package org.ielts.playground.model.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayData {

    private List<ComponentDataResponse> leftContent;
    private List<ComponentDataResponse> rightContent;
    private Map<String, List<String>> listTypeQuestion;

}
