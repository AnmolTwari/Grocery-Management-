package com.shopmanager.repository;

import com.shopmanager.entity.Product;
import com.shopmanager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    boolean existsByOwnerAndSkuIgnoreCase(User owner, String sku);

    boolean existsByOwnerAndSkuIgnoreCaseAndIdNot(User owner, String sku, Long id);

    Optional<Product> findByIdAndOwner(Long id, User owner);


    long countByOwnerAndActiveTrue(User owner);


    @Query("select count(p) from Product p where p.owner = :owner"
            + " and p.active = true"
            + " and p.currentQuantity > 0 and p.currentQuantity <= p.minimumStockLevel")
    long countLowStock(@Param("owner") User owner);


    @Query("select count(p) from Product p where p.owner = :owner"
            + " and p.active = true and p.currentQuantity <= 0")
    long countOutOfStock(@Param("owner") User owner);
}