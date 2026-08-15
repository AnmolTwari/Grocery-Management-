package com.shopmanager.repository;

import com.shopmanager.entity.Category;
import com.shopmanager.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByOwnerAndNameIgnoreCase(User owner, String name);

    Optional<Category> findByIdAndOwner(Long id, User owner);

    boolean existsByOwnerAndNameIgnoreCase(User owner, String name);

    List<Category> findByOwnerOrderByNameAsc(User owner);
}