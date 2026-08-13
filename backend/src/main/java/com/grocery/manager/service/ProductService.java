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
import com.grocery.manager.entity.User;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.CategoryRepository;
import com.grocery.manager.repository.ProductRepository;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.repository.StockMovementRepository;
import com.grocery.manager.security.CurrentUserService;

import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    public ProductService(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StockMovementRepository stockMovementRepository, SaleRepository saleRepository,
            CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.saleRepository = saleRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(String search, Long categoryId,
            StockStatus stockStatus, Pageable pageable) {
        User owner = currentUserService.currentUser();
        return productRepository
                .findAll(buildFilter(owner, search, categoryId, stockStatus), pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        User owner = currentUserService.currentUser();
        assertSkuAvailable(owner, request.sku(), null);
        Product product = new Product(
                owner,
                request.name(),
                findCategory(owner, request.categoryId()),
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
        User owner = currentUserService.currentUser();
        Product product = findProduct(id);
        assertSkuAvailable(owner, request.sku(), id);
        product.setName(request.name());
        product.setCategory(findCategory(owner, request.categoryId()));
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


    @Transactional
    public void deleteProduct(Long id) {
        User owner = currentUserService.currentUser();
        Product product = findProduct(id);
        if (stockMovementRepository.existsByProduct_OwnerAndProductId(owner, id)
                || saleRepository.existsByOwnerAndItems_Product_Id(owner, id)) {
            product.setActive(false);
            return;
        }
        productRepository.delete(product);
    }

    private Specification<Product> buildFilter(User owner, String search, Long categoryId,
            StockStatus stockStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("owner"), owner));
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
        return productRepository.findByIdAndOwner(id, currentUserService.currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found with id " + id));
    }

    private Category findCategory(User owner, Long id) {
        return categoryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND",
                        "Category not found with id " + id));
    }

    private void assertSkuAvailable(User owner, String sku, Long currentId) {
        if (sku == null || sku.isBlank()) {
            return;
        }
        boolean exists = currentId == null
                ? productRepository.existsByOwnerAndSkuIgnoreCase(owner, sku)
                : productRepository.existsByOwnerAndSkuIgnoreCaseAndIdNot(owner, sku, currentId);
        if (exists) {
            throw new DuplicateResourceException("PRODUCT_SKU_EXISTS",
                    "Product with SKU '" + sku + "' already exists", "sku");
        }
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}