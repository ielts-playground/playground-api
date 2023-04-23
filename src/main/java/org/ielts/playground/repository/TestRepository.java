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

    @Query(value = " select aaa.question, aaa.trueAnswer, bbb.userAnswer, aaa.skill " +
            " from (select pa.kei   as question, " +
            "              pa.value as trueAnswer, " +
            "              p.type  as skill " +
            "      from part_answer pa " +
            "      left join part p on pa.part_id = p.id " +
            "      left join test t on p.test_id = t.id " +
            "      left join exam_test et on t.id = et.test_id " +
            "      where et.exam_id = :examId and p.type in :skills) as aaa " +
            " left join (select ea.kei   as question, " +
            "                   ea.value as userAnswer " +
            "           from exam_answer ea " +
            "           left join exam_test et on ea.exam_test_id = et.id " +
            "           where et.exam_id = :examId ) as bbb on aaa.question = bbb.question", nativeQuery = true)
    List<Tuple> getUserAnswerAndTrueAnswer(
            @Param("skills") List<String> skills,
            @Param("examId") Long examId);

}
