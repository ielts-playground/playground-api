package org.ielts.playground.repository;

import org.ielts.playground.model.dto.ExamIdDTO;
import org.ielts.playground.model.entity.ExamEval;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

public interface ExamEvalRepository extends CrudRepository<ExamEval, Long> {

    @Query(value = " SELECT " +
            "    DISTINCT e.id AS exam_id, u.username " +
            "FROM exam e " +
            "         LEFT JOIN exam_eval ee on e.id = ee.exam_id " +
            "         LEFT JOIN user u on u.id = e.user_id " +
            "WHERE " +
            "    ee.writing_point IS NULL OR " +
            "        ee.writing_point = 0 " +
            "ORDER BY e.id DESC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<Tuple> getAllExamIdNotGradedByPage(@Param("pageSize")Long pageSize, @Param("offset")Long offset);

    @Query(value = " SELECT COUNT(distinct e.id) " +
            "   FROM Exam e  " +
            "   LEFT JOIN ExamEval ee on e.id = ee.examId " +
            "   WHERE " +
            "       ee.writingPoint IS NULL OR  " +
            "       ee.writingPoint = 0 " +
            "   ORDER BY e.id DESC ")
    Long getAllExamIdNotGraded();

    @Query(value = " SELECT ee FROM ExamEval ee WHERE ee.examId = :examId ")
    Optional<ExamEval> findByExamId(@Param("examId") Long examId);

    @Query(value = "update ExamEval ee set ee.writingPoint = :point where ee.examId = :examId ")
    void savePointWritingSkillByExamId(@Param("examId")Long examId, @Param("point") Long point);
}
