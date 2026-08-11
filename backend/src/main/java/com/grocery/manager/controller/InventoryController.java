package com.grocery.manager.controller;

import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.grocery.manager.dto.inventory.AdjustmentRequest;
import com.grocery.manager.dto.inventory.StockInRequest;
import com.grocery.manager.dto.inventory.StockMovementResponse;
import com.grocery.manager.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    /** Movement fields that may be used for sorting. */
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "createdAt", "quantityChanged", "newQuantity");

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse stockIn(@Valid @RequestBody StockInRequest request) {
        return inventoryService.stockIn(request);
    }

    @PostMapping("/adjustment")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse adjust(@Valid @RequestBody AdjustmentRequest request) {
        return inventoryService.adjust(request);
    }

    @GetMapping("/movements")
    public Page<StockMovementResponse> listMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return inventoryService.listMovements(productId, toPageable(page, size, sort));
    }

    private Pageable toPageable(int page, int size, String sort) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        Sort sortBy = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        String[] parts = sort.split(",");
        if (parts.length == 2 && SORTABLE_FIELDS.contains(parts[0])) {
            boolean ascending = "asc".equalsIgnoreCase(parts[1]);
            sortBy = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]);
        }
        return PageRequest.of(boundedPage, boundedSize, sortBy);
    }
}