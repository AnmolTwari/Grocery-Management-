package com.shopmanager.controller;

import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanager.dto.common.PageResponse;
import com.shopmanager.dto.sale.SaleRequest;
import com.shopmanager.dto.sale.SaleResponse;
import com.shopmanager.dto.sale.SaleSummaryResponse;
import com.shopmanager.service.SaleService;

@RestController
@RequestMapping("/api/sales")
public class SaleController {


    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "totalAmount", "createdAt");

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }

    @GetMapping
    public PageResponse<SaleSummaryResponse> listSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return saleService.listSales(toPageable(page, size, sort));
    }

    @GetMapping("/{id}")
    public SaleResponse getSale(@PathVariable Long id) {
        return saleService.getSale(id);
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