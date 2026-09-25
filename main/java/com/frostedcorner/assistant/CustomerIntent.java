package com.frostedcorner.assistant;

import java.util.List;

public record CustomerIntent(
        AssistantIntentType intentType,
        String event,
        String flavor,
        Integer servings,
        Integer quantity,
        String faqIntent,
        boolean addAllReferencedRecommendations,
        List<String> keywords) {
}