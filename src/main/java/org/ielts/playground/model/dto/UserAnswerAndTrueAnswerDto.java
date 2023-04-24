package org.ielts.playground.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnswerAndTrueAnswerDto {
    private String question;
    private String trueAnswer;
    private String userAnswer;
    private String skill;
}
