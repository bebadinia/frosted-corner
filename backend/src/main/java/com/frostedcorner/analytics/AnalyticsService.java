package com.frostedcorner.analytics;

import com.frostedcorner.orders.Order;
import com.frostedcorner.orders.OrderItem;
import com.frostedcorner.orders.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private static final Set<String> SUCCESSFUL_STATUSES =
            Set.of("CONFIRMED", "SHIPPED", "DELIVERED");
    private static final int TOP_PRODUCT_LIMIT = 5;
    private static final int AVERAGE_SCALE = 2;

    private final OrderRepository orderRepository;

    public AnalyticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public AnalyticsSummaryResponse getSummary(String storeId) {
        if (storeId != null && storeId.isBlank()) {
            throw new InvalidAnalyticsRequestException("storeId must not be blank");
        }

        List<Order> includedOrders = orderRepository.findAll().stream()
                .filter(this::isSuccessful)
                .filter(order -> storeId == null || storeId.equals(order.getStoreId()))
                .toList();

        BigDecimal revenue = includedOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = includedOrders.size();
        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO.setScale(AVERAGE_SCALE)
                : revenue.divide(BigDecimal.valueOf(totalOrders), AVERAGE_SCALE,
                        RoundingMode.HALF_UP);

        if (totalOrders == 0) {
            revenue = BigDecimal.ZERO.setScale(AVERAGE_SCALE);
        }

        return new AnalyticsSummaryResponse(totalOrders, revenue, averageOrderValue,
                topSellingProducts(includedOrders));
    }

    private boolean isSuccessful(Order order) {
        return SUCCESSFUL_STATUSES.contains(order.getStatus());
    }

    private List<TopSellingProductResponse> topSellingProducts(List<Order> orders) {
        Map<String, ProductSales> salesByProduct = new HashMap<>();

        orders.stream()
                .flatMap(order -> order.getItems().stream())
                .forEach(item -> salesByProduct.compute(item.getProductId(),
                        (productId, currentSales) -> aggregate(currentSales, item)));

        return salesByProduct.values().stream()
                .sorted(Comparator.comparingLong(ProductSales::quantitySold).reversed()
                        .thenComparing(ProductSales::productId))
                .limit(TOP_PRODUCT_LIMIT)
                .map(sales -> new TopSellingProductResponse(sales.productId(), sales.name(),
                        sales.quantitySold()))
                .toList();
    }

    private ProductSales aggregate(ProductSales currentSales, OrderItem item) {
        if (currentSales == null) {
            return new ProductSales(item.getProductId(), item.getProductName(), item.getQuantity());
        }
        return new ProductSales(currentSales.productId(), currentSales.name(),
                currentSales.quantitySold() + item.getQuantity());
    }

    private record ProductSales(String productId, String name, long quantitySold) {
    }
}