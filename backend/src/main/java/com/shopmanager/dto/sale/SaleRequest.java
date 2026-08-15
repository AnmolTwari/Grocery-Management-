package com.shopmanager.dto.sale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;


public record SaleRequest(
        @NotEmpty(message = "A sale must have at least one item")
        List<@Valid SaleItemRequest> items) {
}