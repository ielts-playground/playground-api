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

    @Query(value = " select  " +
            "    aaa.question as question,  " +
            "    bbb.trueAnswer as trueAnswer,  " +
            "    aaa.userAnswer as userAnswer,  " +
            "    aaa.skill as skill  " +
            " from (  " +
            "    select  " +
            "        ea.kei  as question,  " +
            "             ea.value as userAnswer,  " +
            "             p.type   as skill  " +
            "      from exam_answer ea  " +
            "               left join exam_test et on ea.exam_test_id = et.id  " +
            "               left join test t on et.test_id = t.id  " +
            "               left join exam e on et.exam_id = e.id  " +
            "               left join part p on t.id = p.test_id  " +
            "      where et.exam_id = :examId  " +
            "         and p.type in (:skills)  " +
            " ) as aaa  " +
            " left join (  " +
            "     select  " +
            "         pa.kei as question,  " +
            "         null as userAnswer,  " +
            "         pa.value as trueAnswer,  " +
            "         p.type as skill  " +
            "     from part_answer pa  " +
            "     left join part p on pa.part_id = p.id  " +
            "     left join exam_test et on p.test_id = et.test_id  " +
            "     where  " +
            "         et.exam_id = :examId and  " +
            "         p.type in (:skills)  " +
            " ) as bbb on aaa.question = bbb.question and aaa.skill = bbb.skill " +
            " group by aaa.question, aaa.userAnswer, aaa.skill, bbb.trueAnswer ", nativeQuery = true)
    List<Tuple> getUserAnswerAndTrueAnswer(
            @Param("skills") List<String> skills,
            @Param("examId") Long examId);

}
