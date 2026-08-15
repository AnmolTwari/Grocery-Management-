package com.shopmanager.repository;

import com.shopmanager.entity.StockMovement;
import com.shopmanager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @EntityGraph(attributePaths = "product")
    Page<StockMovement> findByProduct_OwnerAndProductId(User owner, Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "product")
    Page<StockMovement> findByProduct_Owner(User owner, Pageable pageable);


    boolean existsByProduct_OwnerAndProductId(User owner, Long productId);
}