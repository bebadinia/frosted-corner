package com.frostedcorner.assistant;

import java.util.List;

public record AssistantCartAction(
        String type,
        boolean openCart,
        List<AssistantCartItem> items) {
}