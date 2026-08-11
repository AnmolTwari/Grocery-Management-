package com.grocery.manager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.product.ProductRequest;
import com.grocery.manager.dto.product.ProductResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.StockStatus;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.CategoryRepository;
import com.grocery.manager.repository.ProductRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(String search, Long categoryId,
            StockStatus stockStatus, Pageable pageable) {
        return productRepository
                .findAll(buildFilter(search, categoryId, stockStatus), pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        assertSkuAvailable(request.sku(), null);
        Product product = new Product(
                request.name(),
                findCategory(request.categoryId()),
                request.brand(),
                request.sku(),
                request.unit(),
                request.purchasePrice(),
                request.sellingPrice(),
                defaultValue(request.currentQuantity()),
                defaultValue(request.minimumStockLevel()),
                request.active() == null || request.active());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        assertSkuAvailable(request.sku(), id);
        product.setName(request.name());
        product.setCategory(findCategory(request.categoryId()));
        product.setBrand(request.brand());
        product.setSku(request.sku());
        product.setUnit(request.unit());
        product.setPurchasePrice(request.purchasePrice());
        product.setSellingPrice(request.sellingPrice());
        product.setCurrentQuantity(defaultValue(request.currentQuantity()));
        product.setMinimumStockLevel(defaultValue(request.minimumStockLevel()));
        product.setActive(request.active() == null || request.active());
        return ProductResponse.from(product);
    }

    /** Soft delete: deactivate so historical data stays intact. */
    @Transactional
    public void deactivateProduct(Long id) {
        findProduct(id).setActive(false);
    }

    private Specification<Product> buildFilter(String search, Long categoryId,
            StockStatus stockStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like)));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (stockStatus != null) {
                switch (stockStatus) {
                    case OUT_OF_STOCK -> predicates
                            .add(cb.lessThanOrEqualTo(root.get("currentQuantity"), BigDecimal.ZERO));
                    case LOW_STOCK -> {
                        predicates.add(cb.greaterThan(root.get("currentQuantity"), BigDecimal.ZERO));
                        predicates.add(cb.lessThanOrEqualTo(root.get("currentQuantity"),
                                root.get("minimumStockLevel")));
                    }
                    case IN_STOCK -> predicates.add(cb.greaterThan(root.get("currentQuantity"),
                            root.get("minimumStockLevel")));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with id " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND",
                        "Category not found with id " + id));
    }

    private void assertSkuAvailable(String sku, Long currentId) {
        if (sku == null || sku.isBlank()) {
            return;
        }
        boolean exists = currentId == null
                ? productRepository.existsBySkuIgnoreCase(sku)
                : productRepository.existsBySkuIgnoreCaseAndIdNot(sku, currentId);
        if (exists) {
            throw new DuplicateResourceException("PRODUCT_SKU_EXISTS",
                    "Product with SKU '" + sku + "' already exists", "sku");
        }
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}