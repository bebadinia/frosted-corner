package com.frostedcorner.assistant;

public record AssistantOffer(
        boolean eligible,
        Integer discountPercent,
        String message) {
}