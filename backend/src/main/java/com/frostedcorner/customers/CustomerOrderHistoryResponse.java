package com.frostedcorner.customers;

import com.frostedcorner.orders.Order;
import java.util.List;

public record CustomerOrderHistoryResponse(
        List<Order> orders,
        List<FavoriteItemResponse> favoriteItems) {
}
