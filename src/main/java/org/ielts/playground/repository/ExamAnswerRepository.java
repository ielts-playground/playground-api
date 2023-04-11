package org.ielts.playground.repository;

import org.ielts.playground.model.entity.ExamAnswer;
import org.springframework.data.repository.CrudRepository;

public interface ExamAnswerRepository extends CrudRepository<ExamAnswer, Long> {
    boolean existsByExamPartId(Long examPartId);
}
