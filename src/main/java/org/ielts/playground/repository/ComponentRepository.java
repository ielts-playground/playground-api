package org.ielts.playground.repository;

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
            " WHERE p.testId = :testId ")
    List<ComponentWithPartNumber> findByTestId(@Param("testId") Long testId);
}
