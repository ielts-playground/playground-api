package org.ielts.playground.repository;

import org.ielts.playground.model.entity.ExamTest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExamTestRepository extends CrudRepository<ExamTest, Long> {

    @Query(value = " select et.testId from ExamTest et where et.examId = :examId")
    Long getTestIdByExamId(@Param("examId")Long examId);
}
