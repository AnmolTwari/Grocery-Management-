package com.grocery.manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grocery.manager.entity.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /** Stock movements for a single product, newest-first when sorted by createdAt desc. */
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
}