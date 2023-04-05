package org.ielts.playground.repository;

import org.ielts.playground.model.entity.Component;
import org.springframework.data.repository.CrudRepository;

public interface ComponentRepository extends CrudRepository<Component, Long> {
}
