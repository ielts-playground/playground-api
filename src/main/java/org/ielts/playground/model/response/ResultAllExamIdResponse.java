package org.ielts.playground.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ielts.playground.model.dto.ExamIdDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultAllExamIdResponse {

    private Long page;
    private Long size;
    private Long total;
    private List<ExamIdDTO> examIds;
 }
