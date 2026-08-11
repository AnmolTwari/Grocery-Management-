package com.grocery.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.sale.SaleItemRequest;
import com.grocery.manager.dto.sale.SaleRequest;
import com.grocery.manager.dto.sale.SaleResponse;
import com.grocery.manager.dto.sale.SaleSummaryResponse;
import com.grocery.manager.entity.MovementType;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.Sale;
import com.grocery.manager.entity.SaleItem;
import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.exception.InsufficientStockException;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.ProductRepository;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.repository.StockMovementRepository;

/**
 * Sales business rules. Creating a sale is one transaction: validate
 * availability, snapshot prices, calculate totals, create the sale and
 * line items, reduce stock and record movements — all-or-nothing.
 */
@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        requireItems(request.items());
        Map<Long, Product> products = new HashMap<>();
        Map<Long, BigDecimal> soldQuantities = new LinkedHashMap<>();
        List<SaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest line : request.items()) {
            Product product = products.computeIfAbsent(line.productId(), this::findProduct);
            BigDecimal quantity = line.quantity();
            soldQuantities.merge(product.getId(), quantity, BigDecimal::add);
            items.add(new SaleItem(product, quantity, product.getSellingPrice(),
                    product.getPurchasePrice()));
            total = total.add(money(product.getSellingPrice().multiply(quantity)));
        }

        assertStockAvailable(products, soldQuantities);

        Sale sale = saleRepository.save(new Sale(items, total));

        for (Map.Entry<Long, BigDecimal> sold : soldQuantities.entrySet()) {
            Product product = products.get(sold.getKey());
            BigDecimal previous = product.getCurrentQuantity();
            BigDecimal updated = previous.subtract(sold.getValue());
            product.setCurrentQuantity(updated);
            stockMovementRepository.save(new StockMovement(product, MovementType.SALE,
                    previous, sold.getValue().negate(), updated,
                    "Sale #" + sale.getId()));
        }

        return SaleResponse.from(sale);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSale(Long id) {
        return SaleResponse.from(findSale(id));
    }

    @Transactional(readOnly = true)
    public Page<SaleSummaryResponse> listSales(Pageable pageable) {
        return saleRepository.findAll(pageable).map(SaleSummaryResponse::from);
    }

    private void requireItems(List<SaleItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A sale must have at least one item");
        }
    }

    private void assertStockAvailable(Map<Long, Product> products,
            Map<Long, BigDecimal> soldQuantities) {
        for (Map.Entry<Long, BigDecimal> sold : soldQuantities.entrySet()) {
            Product product = products.get(sold.getKey());
            BigDecimal available = product.getCurrentQuantity();
            BigDecimal required = sold.getValue();
            if (available.compareTo(required) < 0) {
                throw new InsufficientStockException("INSUFFICIENT_STOCK",
                        "Not enough stock for " + product.getName() + " (available " + available
                                + ", required " + required + ")");
            }
        }
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Sale findSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SALE_NOT_FOUND",
                        "Sale not found with id " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with id " + id));
    }
}