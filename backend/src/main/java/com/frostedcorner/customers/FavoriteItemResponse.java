package com.frostedcorner.customers;

public record FavoriteItemResponse(
        String productId,
        String productName,
        long quantityOrdered) {
}
