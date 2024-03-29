package org.ielts.playground.repository;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.entity.Component;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComponentRepository extends CrudRepository<Component, Long> {
    @Query(value = " SELECT c AS component, p.number as partNumber " +
            " FROM Component c " +
            " JOIN Part p ON p.id = c.partId " +
            " WHERE p.testId = :testId AND p.type = :type ")
    List<ComponentWithPartNumber> findByTestIdAndType(@Param("testId") Long testId, @Param("type") PartType type);

    @Query(value = " SELECT c AS component, p.number as partNumber " +
            " FROM Component c " +
            " LEFT JOIN Part p ON p.id = c.partId " +
            " LEFT JOIN Test t on p.testId = t.id" +
            " LEFT JOIN ExamTest et ON et.testId = p.testId " +
            " WHERE et.examId = :examId AND p.type = :partType" +
            " GROUP BY c.id, p.type, p.number")
    List<ComponentWithPartNumber> findByExamIdAndPartType(
            @Param("examId") Long examId,
            @Param("partType") PartType partType);
}
