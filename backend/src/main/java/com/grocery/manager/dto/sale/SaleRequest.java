package com.grocery.manager.dto.sale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/** Request body for creating a sale. */
public record SaleRequest(
        @NotEmpty(message = "A sale must have at least one item")
        List<@Valid SaleItemRequest> items) {
}