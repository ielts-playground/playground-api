package org.ielts.playground.repository;

import org.ielts.playground.model.entity.ExamPart;
import org.springframework.data.repository.CrudRepository;

public interface ExamPartRepository extends CrudRepository<ExamPart, Long> {
}
