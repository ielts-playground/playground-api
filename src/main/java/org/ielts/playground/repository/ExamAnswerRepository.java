package org.ielts.playground.repository;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.entity.ExamAnswer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamAnswerRepository extends CrudRepository<ExamAnswer, Long> {
    boolean existsByExamPartId(Long examPartId);

    @Query(value = " SELECT ea FROM ExamAnswer ea " +
            " LEFT JOIN ExamTest et ON et.id = ea.examTestId " +
            " WHERE " +
            "   et.examId = :examId AND " +
            "   et.testId IN ( " +
            "     SELECT DISTINCT p.testId " +
            "     FROM Part p " +
            "     WHERE p.testId = et.testId AND p.type = :partType " +
            "   ) ")
    List<ExamAnswer> findByExamIdAndPartType(
            @Param("examId") Long examId,
            @Param("partType") PartType partType);
}
