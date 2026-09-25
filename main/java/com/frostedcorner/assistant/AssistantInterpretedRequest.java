package com.frostedcorner.assistant;

import java.util.List;

public record AssistantInterpretedRequest(
        String intent,
        String event,
        String flavor,
        Integer servings,
        Integer quantity,
        List<String> preferences) {
}