package com.frostedcorner.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryQuantityUpdate(
        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must be zero or greater")
        Integer quantity) {
}