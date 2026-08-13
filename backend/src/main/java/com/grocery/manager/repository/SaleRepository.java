package com.grocery.manager.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grocery.manager.entity.Sale;
import com.grocery.manager.entity.User;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findByOwner(User owner, Pageable pageable);

    java.util.Optional<Sale> findByIdAndOwner(Long id, User owner);

    /** Whether any sale line of this owner references the product (used for safe deletion). */
    boolean existsByOwnerAndItems_Product_Id(User owner, Long productId);

    /** Number of sale transactions recorded at or after the given time. */
    long countByOwnerAndCreatedAtGreaterThanEqual(User owner, LocalDateTime from);

    /** Number of sale transactions within the given time window. */
    long countByOwnerAndCreatedAtBetween(User owner, LocalDateTime from, LocalDateTime to);

    /** Sum of the charged totals of sales at or after the given time. */
    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s"
            + " where s.owner = :owner and s.createdAt >= :from")
    BigDecimal sumTotalAmountSince(@Param("owner") User owner, @Param("from") LocalDateTime from);

    /** Sum of the charged totals of sales within the given time window. */
    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s"
            + " where s.owner = :owner and s.createdAt >= :from and s.createdAt < :to")
    BigDecimal sumTotalAmountBetween(@Param("owner") User owner, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Estimated profit (selling price - purchase price) on line items at or after the given time. */
    @Query("select coalesce(sum((si.unitPrice - si.purchasePrice) * si.quantity), 0)"
            + " from SaleItem si where si.sale.owner = :owner and si.sale.createdAt >= :from")
    BigDecimal sumEstimatedProfitSince(@Param("owner") User owner, @Param("from") LocalDateTime from);

    /** Estimated profit on line items within the given time window. */
    @Query("select coalesce(sum((si.unitPrice - si.purchasePrice) * si.quantity), 0)"
            + " from SaleItem si where si.sale.owner = :owner"
            + " and si.sale.createdAt >= :from and si.sale.createdAt < :to")
    BigDecimal sumEstimatedProfitBetween(@Param("owner") User owner, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}