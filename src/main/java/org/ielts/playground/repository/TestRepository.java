package org.ielts.playground.repository;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.entity.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Tuple;
import java.util.List;

public interface TestRepository extends CrudRepository<Test, Long> {
    @Transactional
    @Modifying
    @Query(value = " UPDATE Test t SET t.active = false WHERE t.id = :id ")
    void deactivateById(@Param("id") Long id);

    @Query(value = " SELECT t.id FROM Part p join Test t on t.id = p.testId " +
            " where t.active = true" +
            " and p.type = :skill ")
    List<Long> allActiveTestIds(@Param("skill") PartType skill);

    @Query(value = " SELECT p.id FROM Part p where p.testId = :testId")
    List<Long> getAllPartIdByTestId(@Param("testId") Long testId);

    @Query(value = " select pa.kei, pa.value as true_answer, ea.value as user_answer " +
            "    from Exam e " +
            "   join ExamTest et on e.id = et.examId " +
            "   join Part p on et.testId = p.testId " +
            "   join PartAnswer pa on p.id = pa.partId " +
            "   join ExamAnswer ea on et.id = ea.examTestId " +
            "   where et.examId = :examId")
    List<Tuple> getUserAnswerAndTrueAnswer(@Param("examId") Long examId);

}
