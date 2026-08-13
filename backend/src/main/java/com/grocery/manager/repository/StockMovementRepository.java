package com.grocery.manager.repository;

import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /** Stock movements for a single product of an owner, newest-first when sorted by createdAt desc. */
    Page<StockMovement> findByProduct_OwnerAndProductId(User owner, Long productId, Pageable pageable);

    /** All stock movements of an owner. */
    Page<StockMovement> findByProduct_Owner(User owner, Pageable pageable);

    /** Whether any stock movement of this owner references the product (used for safe deletion). */
    boolean existsByProduct_OwnerAndProductId(User owner, Long productId);
}