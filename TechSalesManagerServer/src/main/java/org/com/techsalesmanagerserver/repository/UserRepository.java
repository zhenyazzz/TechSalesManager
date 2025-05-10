package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByIdBetween(Long startId, Long endId);
    List<User> findByEmailContainingIgnoreCase(String trim);
}
