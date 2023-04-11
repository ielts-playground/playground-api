package org.ielts.playground.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ielts.playground.common.enumeration.ComponentType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentDataResponse {

    private Long id;
    private Long subId;
    private Long part;
    private String type;
    private Long numberOrder;
    private String questionTitle;
    private String text;
    private String lastText;
    private Boolean isDownLine;
    private List<OptionResponse> options;

    @JsonIgnore
    private ComponentType componentType;
}
