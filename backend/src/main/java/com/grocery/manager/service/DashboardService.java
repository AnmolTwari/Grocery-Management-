package com.grocery.manager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.dashboard.DashboardSummaryResponse;
import com.grocery.manager.dto.sale.SaleSummaryResponse;
import com.grocery.manager.entity.Sale;
import com.grocery.manager.entity.User;
import com.grocery.manager.repository.ProductRepository;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.security.CurrentUserService;


@Service
public class DashboardService {

    private static final int RECENT_SALES_LIMIT = 5;

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(SaleRepository saleRepository, ProductRepository productRepository,
            CurrentUserService currentUserService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        User owner = currentUserService.currentUser();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Page<Sale> recent = saleRepository.findByOwner(owner, PageRequest.of(0, RECENT_SALES_LIMIT,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))));
        List<SaleSummaryResponse> recentSales =
                recent.getContent().stream().map(SaleSummaryResponse::from).toList();

        return new DashboardSummaryResponse(
                saleRepository.countByOwnerAndCreatedAtGreaterThanEqual(owner, startOfToday),
                saleRepository.sumTotalAmountSince(owner, startOfToday),
                saleRepository.sumEstimatedProfitSince(owner, startOfToday),
                productRepository.countByOwnerAndActiveTrue(owner),
                productRepository.countLowStock(owner),
                productRepository.countOutOfStock(owner),
                recentSales);
    }
}