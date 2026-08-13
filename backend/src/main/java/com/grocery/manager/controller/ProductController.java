package com.grocery.manager.controller;

import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.grocery.manager.dto.product.ProductRequest;
import com.grocery.manager.dto.product.ProductResponse;
import com.grocery.manager.entity.StockStatus;
import com.grocery.manager.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    /** Product fields that may be used for sorting. */
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "name", "sku", "purchasePrice", "sellingPrice", "currentQuantity",
                    "minimumStockLevel", "createdAt");

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductResponse> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        return productService.listProducts(search, categoryId, stockStatus, toPageable(page, size, sort));
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    /**
     * Removes a product: hard-deletes it when it has no sale/stock
     * history, otherwise deactivates it so historical records survive.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    private Pageable toPageable(int page, int size, String sort) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        Sort sortBy = defaultSort();
        String[] parts = sort.split(",");
        if (parts.length == 2 && SORTABLE_FIELDS.contains(parts[0])) {
            boolean ascending = "asc".equalsIgnoreCase(parts[1]);
            sortBy = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]);
        }
        return PageRequest.of(boundedPage, boundedSize, sortBy);
    }

    private Sort defaultSort() {
        return Sort.by(Sort.Direction.DESC, "id");
    }
}