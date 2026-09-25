package com.frostedcorner.assistant;

import java.math.BigDecimal;

public record AssistantRecommendation(
        String productId,
        String name,
        BigDecimal price,
        String category,
        String imageFileName,
        Integer suggestedQuantity,
        String reason) {
}