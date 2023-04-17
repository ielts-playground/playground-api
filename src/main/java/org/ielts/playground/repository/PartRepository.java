package org.ielts.playground.repository;

import org.ielts.playground.model.entity.Part;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface PartRepository extends CrudRepository<Part, Long> {
    @Query(value = "select p.number from Part p where p.id =:partId")
    Long getPartNumberByPartId(@Param("partId") Long partId);
}
