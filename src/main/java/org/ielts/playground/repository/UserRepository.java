package org.ielts.playground.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.ielts.playground.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);

    @Query(value = " SELECT u FROM User u WHERE u.id IN ( " +
            "   SELECT e.userId FROM Exam e WHERE e.id = :examId) ")
    Optional<User> findByExamId(@Param("examId") Long examId);
}
