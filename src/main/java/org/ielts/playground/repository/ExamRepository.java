package org.ielts.playground.repository;

import org.ielts.playground.model.entity.Exam;
import org.springframework.data.repository.CrudRepository;

public interface ExamRepository extends CrudRepository<Exam, Long> {
}
