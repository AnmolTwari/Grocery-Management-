package com.shopmanager.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopmanager.entity.Product;
import com.shopmanager.entity.Sale;
import com.shopmanager.entity.User;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = "items")
    Page<Sale> findByOwner(User owner, Pageable pageable);

    @EntityGraph(attributePaths = { "items", "items.product" })
    java.util.Optional<Sale> findByIdAndOwner(Long id, User owner);


    boolean existsByOwnerAndItems_Product_Id(User owner, Long productId);


    long countByOwner(User owner);


    long countByOwnerAndCreatedAtGreaterThanEqual(User owner, LocalDateTime from);


    long countByOwnerAndCreatedAtBetween(User owner, LocalDateTime from, LocalDateTime to);


    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s"
            + " where s.owner = :owner and s.createdAt >= :from")
    BigDecimal sumTotalAmountSince(@Param("owner") User owner, @Param("from") LocalDateTime from);


    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s"
            + " where s.owner = :owner and s.createdAt >= :from and s.createdAt < :to")
    BigDecimal sumTotalAmountBetween(@Param("owner") User owner, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);


    @Query("select coalesce(sum((si.unitPrice - si.purchasePrice) * si.quantity), 0)"
            + " from SaleItem si where si.sale.owner = :owner and si.sale.createdAt >= :from")
    BigDecimal sumEstimatedProfitSince(@Param("owner") User owner, @Param("from") LocalDateTime from);


    @Query("select coalesce(sum((si.unitPrice - si.purchasePrice) * si.quantity), 0)"
            + " from SaleItem si where si.sale.owner = :owner"
            + " and si.sale.createdAt >= :from and si.sale.createdAt < :to")
    BigDecimal sumEstimatedProfitBetween(@Param("owner") User owner, @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);


    long countByCreatedAtGreaterThanEqual(LocalDateTime from);


    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s")
    BigDecimal sumTotalAmountAll();


    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.createdAt >= :from")
    BigDecimal sumTotalAmountSince(@Param("from") LocalDateTime from);

    @Query("""
            select si.product from SaleItem si
            where si.sale.owner = :owner and si.product.active = true
            group by si.product
            order by sum(si.quantity) desc
            """)
    java.util.List<Product> findTopSoldProducts(@Param("owner") User owner, Pageable pageable);

    @Query("""
            select function('date', s.createdAt), coalesce(sum(s.totalAmount), 0)
            from Sale s
            where s.owner = :owner and s.createdAt >= :from
            group by function('date', s.createdAt)
            order by function('date', s.createdAt) asc
            """)
    java.util.List<Object[]> sumDailyRevenue(@Param("owner") User owner, @Param("from") LocalDateTime from);
}