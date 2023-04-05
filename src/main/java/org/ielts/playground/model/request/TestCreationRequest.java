package org.ielts.playground.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ielts.playground.model.entity.type.Map;
import org.ielts.playground.model.entity.type.Size;
import org.ielts.playground.model.entity.type.Text;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestCreationRequest {
    @NotNull
    private String skill;

    @NotNull
    private List<PartComponent> components;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PartComponent {
        private Long number;
        private String type;
        private String kei;
        private Text value;
        private Size size;
        private Map options;
    }
}
