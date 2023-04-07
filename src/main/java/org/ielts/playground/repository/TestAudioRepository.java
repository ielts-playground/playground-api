package org.ielts.playground.repository;

import org.ielts.playground.model.entity.TestAudio;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TestAudioRepository extends CrudRepository<TestAudio, Long> {
    Optional<TestAudio> findOneByTestId(Long testId);
}
