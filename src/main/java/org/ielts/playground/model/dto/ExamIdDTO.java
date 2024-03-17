package org.ielts.playground.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExamIdDTO {

    private Long examId;
    private String userName;
    private String firstName;
    private String lastName;
    private String createdAt;
}
