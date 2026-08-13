package com.grocery.manager.repository;

import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByOwnerAndSkuIgnoreCase(User owner, String sku);

    boolean existsByOwnerAndSkuIgnoreCaseAndIdNot(User owner, String sku, Long id);

    Optional<Product> findByIdAndOwner(Long id, User owner);

    /** Total active (non-hidden) products for an owner. */
    long countByOwnerAndActiveTrue(User owner);

    /** Active products with stock above zero but at or below the minimum level. */
    @Query("select count(p) from Product p where p.owner = :owner"
            + " and p.active = true"
            + " and p.currentQuantity > 0 and p.currentQuantity <= p.minimumStockLevel")
    long countLowStock(@Param("owner") User owner);

    /** Active products with no stock left. */
    @Query("select count(p) from Product p where p.owner = :owner"
            + " and p.active = true and p.currentQuantity <= 0")
    long countOutOfStock(@Param("owner") User owner);
}