package org.ielts.playground.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.ielts.playground.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
}
