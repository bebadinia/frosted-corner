package com.frostedcorner.analytics;

public record TopSellingProductResponse(String productId, String name, long quantitySold) {
}