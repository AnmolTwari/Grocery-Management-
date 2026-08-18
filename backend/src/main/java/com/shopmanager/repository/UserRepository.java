package com.shopmanager.repository;

import com.shopmanager.entity.User;
import com.shopmanager.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRole(UserRole role);
    long countByRole(UserRole role);
    long countByEnabledTrue();
    Optional<User> findFirstByOrderByIdAsc();
}