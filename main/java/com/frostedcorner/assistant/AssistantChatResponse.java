package com.frostedcorner.assistant;

import java.util.List;

public record AssistantChatResponse(
        AssistantInterpretedRequest interpretedRequest,
        String message,
        String suggestedAction,
        AssistantOffer offer,
        List<AssistantRecommendation> recommendations,
        AssistantCartAction cartAction) {
}