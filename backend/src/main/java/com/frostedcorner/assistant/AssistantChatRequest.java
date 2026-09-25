package com.frostedcorner.assistant;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AssistantChatRequest(
        @NotBlank(message = "customerId is required")
        String customerId,
        String storeId,
        @NotBlank(message = "message is required")
        String message,
        List<String> recommendedProductIds) {
}