package com.shopmanager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.common.PageResponse;
import com.shopmanager.dto.product.ProductRequest;
import com.shopmanager.dto.product.ProductResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.Product;
import com.shopmanager.entity.StockStatus;
import com.shopmanager.entity.Unit;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.repository.CategoryRepository;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.repository.StockMovementRepository;
import com.shopmanager.security.CurrentUserService;

import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private static final Set<Unit> COUNT_UNITS =
            Set.of(Unit.PIECE, Unit.PACKET, Unit.BOX, Unit.BOTTLE);

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
    public PageResponse<ProductResponse> listProducts(String search, Long categoryId,
            StockStatus stockStatus, Pageable pageable) {
        User owner = currentUserService.currentUser();
        return PageResponse.from(productRepository
                .findAll(buildFilter(owner, search, categoryId, stockStatus), pageable)
                .map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listPopularProducts(int limit) {
        User owner = currentUserService.currentUser();
        int bounded = Math.min(Math.max(limit, 1), 20);
        return saleRepository.findTopSoldProducts(owner, PageRequest.of(0, bounded))
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        User owner = currentUserService.currentUser();
        assertSkuAvailable(owner, request.sku(), null);
        validateQuantities(request.unit(), defaultValue(request.currentQuantity()),
                defaultValue(request.minimumStockLevel()));
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
        validateQuantities(request.unit(), defaultValue(request.currentQuantity()),
                defaultValue(request.minimumStockLevel()));
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
    public boolean deleteProduct(Long id) {
        User owner = currentUserService.currentUser();
        Product product = findProduct(id);
        if (stockMovementRepository.existsByProduct_OwnerAndProductId(owner, id)
                || saleRepository.existsByOwnerAndItems_Product_Id(owner, id)) {
            product.setActive(false);
            return true;
        }
        productRepository.delete(product);
        return false;
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

    private void validateQuantities(Unit unit, BigDecimal currentQuantity,
            BigDecimal minimumStockLevel) {
        if (COUNT_UNITS.contains(unit)) {
            requireWholeNumber(currentQuantity, "Current quantity");
            requireWholeNumber(minimumStockLevel, "Minimum stock level");
        }
    }

    private void requireWholeNumber(BigDecimal value, String label) {
        if (value.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(label
                    + " must be a whole number for this product unit");
        }
    }
}