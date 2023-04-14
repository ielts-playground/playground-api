package org.ielts.playground.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ielts.playground.model.entity.type.Map;
import org.ielts.playground.model.entity.type.Text;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

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

    @NotNull
    private List<PartAnswer> answers;

    @Nullable
    private MultipartFile audio;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PartComponent {
        private Long part;
        private String type;
        private String kei;
        private Text value;
        private Text size;
        private Map options;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PartAnswer {
        private Long part;
        private String kei;
        private String value;
    }
}
