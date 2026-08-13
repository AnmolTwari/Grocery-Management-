package com.grocery.manager.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.inventory.AdjustmentRequest;
import com.grocery.manager.dto.inventory.StockInRequest;
import com.grocery.manager.dto.inventory.StockMovementResponse;
import com.grocery.manager.entity.MovementType;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.entity.User;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.ProductRepository;
import com.grocery.manager.repository.StockMovementRepository;
import com.grocery.manager.security.CurrentUserService;

/**
 * Inventory business rules. Every stock change updates the product and
 * records a traceable StockMovement atomically.
 */
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CurrentUserService currentUserService;

    public InventoryService(ProductRepository productRepository,
            StockMovementRepository stockMovementRepository,
            CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.currentUserService = currentUserService;
    }

    /** Adds stock to a product and records the change. */
    @Transactional
    public StockMovementResponse stockIn(StockInRequest request) {
        Product product = findProduct(request.productId());
        BigDecimal previous = product.getCurrentQuantity();
        BigDecimal updated = previous.add(request.quantity());
        product.setCurrentQuantity(updated);
        return StockMovementResponse.from(saveMovement(product, MovementType.STOCK_IN,
                previous, request.quantity(), updated, request.reason()));
    }

    /** Corrects a product's stock to an absolute value and records the change. */
    @Transactional
    public StockMovementResponse adjust(AdjustmentRequest request) {
        Product product = findProduct(request.productId());
        BigDecimal previous = product.getCurrentQuantity();
        BigDecimal updated = request.newQuantity();
        BigDecimal changed = updated.subtract(previous);
        product.setCurrentQuantity(updated);
        return StockMovementResponse.from(saveMovement(product, MovementType.ADJUSTMENT,
                previous, changed, updated, request.reason()));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> listMovements(Long productId, Pageable pageable) {
        User owner = currentUserService.currentUser();
        Page<StockMovement> page = productId == null
                ? stockMovementRepository.findByProduct_Owner(owner, pageable)
                : stockMovementRepository.findByProduct_OwnerAndProductId(owner, productId,
                        pageable);
        return page.map(StockMovementResponse::from);
    }

    private StockMovement saveMovement(Product product, MovementType type,
            BigDecimal previous, BigDecimal changed, BigDecimal updated, String reason) {
        return stockMovementRepository.save(
                new StockMovement(product, type, previous, changed, updated, reason));
    }

    private Product findProduct(Long id) {
        return productRepository.findByIdAndOwner(id, currentUserService.currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with id " + id));
    }
}