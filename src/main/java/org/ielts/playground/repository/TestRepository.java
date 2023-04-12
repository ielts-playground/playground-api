package org.ielts.playground.repository;

import org.ielts.playground.common.enumeration.PartType;
import org.ielts.playground.model.dto.TestWithRate;
import org.ielts.playground.model.entity.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface TestRepository extends CrudRepository<Test, Long> {
    @Transactional
    @Modifying
    @Query(value = " UPDATE Test t SET t.active = false WHERE t.id = :id ")
    void deactivateById(@Param("id") Long id);

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query(nativeQuery = true, value = " SELECT " +
            "    DISTINCT t.id AS id," +
            "    DATEDIFF(t.created_at, DATE_ADD(NOW(), INTERVAL -:x DAY)) / :x AS rate " +
            " FROM test AS t " +
            " JOIN part AS p ON t.id = p.test_id " +
            " WHERE " +
            "    t.active = 1 AND " +
            "    p.type = :#{#type.value} AND " +
            "    DATEDIFF(t.created_at, DATE_ADD(NOW(), INTERVAL -:x DAY)) > 0 " +
            " ORDER BY " +
            "    rate DESC," +
            "    id DESC ")
    List<TestWithRate> getTestWithRateInXDays(
            @NotNull @Param("x") Long x,
            @NotNull @Param("type") PartType type);
}
