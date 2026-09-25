package com.frostedcorner.assistant;

import java.math.BigDecimal;

public record AssistantCartItem(
        String productId,
        String name,
        BigDecimal price,
        String category,
        String imageFileName,
        Integer quantity) {
}