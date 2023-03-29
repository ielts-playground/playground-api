package org.ielts.playground.model.entity;

import lombok.*;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "exam_answer")
public class ExamAnswer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "number")
    private Long number;

    @Column(name = "value")
    private String value;

}
