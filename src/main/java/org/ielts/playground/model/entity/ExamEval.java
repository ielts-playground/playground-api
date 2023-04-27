package org.ielts.playground.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "exam_eval")
public class ExamEval extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "writing_point")
    private Long writingPoint;

    @Column(name = "reading_point")
    private Long readingPoint;

    @Column(name = "listening_point")
    private Long listeningPoint;

    @Column(name = "created_by")
    private Long createdBy;

}
