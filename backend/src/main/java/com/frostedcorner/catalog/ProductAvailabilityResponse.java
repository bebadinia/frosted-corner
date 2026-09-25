package com.frostedcorner.catalog;

public record ProductAvailabilityResponse(
        String productId,
        int quantity,
        int lowStockThreshold) {
}
