package org.ielts.playground.repository;

import org.ielts.playground.common.enumeration.ComponentType;
import org.ielts.playground.model.dto.ComponentWithPartNumber;
import org.ielts.playground.model.entity.Component;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface ComponentRepository extends CrudRepository<Component, Long> {
    @Query(value = " SELECT " +
            "    c AS component," +
            "    p.number AS partNumber " +
            " FROM Component AS c " +
            " JOIN Part AS p ON p.id = c.partId " +
            " WHERE p.testId = :testId AND c.type NOT IN :excludedTypes ")
    List<ComponentWithPartNumber> findAllByTestId(
            @NotNull @Param("testId") Long testId,
            @NotNull List<ComponentType> excludedTypes);

    @Query(value = " SELECT " +
            "    c AS component," +
            "    p.number AS partNumber " +
            " FROM Component AS c " +
            " JOIN Part AS p ON p.id = c.partId " +
            " WHERE p.testId = :testId AND c.type = :type ")
    List<ComponentWithPartNumber> findAllByTestIdAndType(
            @NotNull @Param("testId") Long testId,
            @NotNull @Param("type") ComponentType type);

    default List<ComponentWithPartNumber> findAllRangesByTestId(@NotNull Long testId) {
        return this.findAllByTestIdAndType(testId, ComponentType.RANGE);
    }

    @Query(value = " SELECT " +
            "    c AS component," +
            "    p.number AS partNumber " +
            " FROM Component AS c " +
            " JOIN Part AS p ON p.id = c.partId " +
            " WHERE " +
            "    p.testId = :testId AND " +
            "    c.type = :#{T(org.ielts.playground.common.enumeration.ComponentType).QUESTION} AND " +
            "    c.options IS NOT NULL ")
    List<ComponentWithPartNumber> findAllAnswerSelectableQuestionsByTestId(
            @NotNull @Param("testId") Long testId);
}
