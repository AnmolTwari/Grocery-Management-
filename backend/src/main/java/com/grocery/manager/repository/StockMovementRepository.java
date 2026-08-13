package com.grocery.manager.repository;

import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {


    Page<StockMovement> findByProduct_OwnerAndProductId(User owner, Long productId, Pageable pageable);


    Page<StockMovement> findByProduct_Owner(User owner, Pageable pageable);


    boolean existsByProduct_OwnerAndProductId(User owner, Long productId);
}