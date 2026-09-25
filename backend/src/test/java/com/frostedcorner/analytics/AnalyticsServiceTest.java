package com.frostedcorner.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frostedcorner.orders.Order;
import com.frostedcorner.orders.OrderItem;
import com.frostedcorner.orders.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void calculatesMetricsFromSuccessfulOrdersAndExcludesOtherStatuses() {
        Order confirmed = order("o1", "store1", "CONFIRMED", "10.00", List.of(
                item("P001", "Chocolate Cake", 2),
                item("P002", "Strawberry Cake", 1)));
        Order shipped = order("o2", "store1", "SHIPPED", "20.00", List.of(
                item("P001", "Chocolate Cake", 3),
                item("P003", "Red Velvet Cake", 6)));
        Order delivered = order("o3", "store2", "DELIVERED", "70.00", List.of(
                item("P002", "Strawberry Cake", 4),
                item("P004", "Vanilla Cake", 6)));
        Order pending = order("o4", "store1", "PENDING", "999.00",
                List.of(item("P005", "Cupcake", 100)));
        Order cancelled = order("o5", "store1", "CANCELLED", "999.00",
                List.of(item("P005", "Cupcake", 100)));
        when(orderRepository.findAll())
                .thenReturn(List.of(confirmed, shipped, delivered, pending, cancelled));

        AnalyticsSummaryResponse result = analyticsService.getSummary(null);

        assertEquals(3, result.totalOrders());
        assertEquals(new BigDecimal("100.00"), result.revenue());
        assertEquals(new BigDecimal("33.33"), result.averageOrderValue());
        assertEquals(List.of(
                new TopSellingProductResponse("P003", "Red Velvet Cake", 6),
                new TopSellingProductResponse("P004", "Vanilla Cake", 6),
                new TopSellingProductResponse("P001", "Chocolate Cake", 5),
                new TopSellingProductResponse("P002", "Strawberry Cake", 5)),
                result.topSellingProducts());
    }

    @Test
    void filtersMetricsByStoreId() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("o1", "store1", "CONFIRMED", "10.00",
                        List.of(item("P001", "Chocolate Cake", 2))),
                order("o2", "store2", "DELIVERED", "20.00",
                        List.of(item("P002", "Strawberry Cake", 4))),
                order("o3", "store1", "PENDING", "30.00",
                        List.of(item("P003", "Red Velvet Cake", 6)))));

        AnalyticsSummaryResponse result = analyticsService.getSummary("store1");

        assertEquals(1, result.totalOrders());
        assertEquals(new BigDecimal("10.00"), result.revenue());
        assertEquals(new BigDecimal("10.00"), result.averageOrderValue());
        assertEquals(List.of(new TopSellingProductResponse(
                "P001", "Chocolate Cake", 2)), result.topSellingProducts());
    }

    @Test
    void roundsAverageOrderValueHalfUp() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("o1", "store1", "CONFIRMED", "10.00", List.of()),
                order("o2", "store1", "CONFIRMED", "10.01", List.of())));

        AnalyticsSummaryResponse result = analyticsService.getSummary(null);

        assertEquals(new BigDecimal("20.01"), result.revenue());
        assertEquals(new BigDecimal("10.01"), result.averageOrderValue());
    }

    @Test
    void returnsZeroValuesAndNoProductsWhenThereAreNoIncludedOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("o1", "store1", "CANCELLED", "10.00",
                        List.of(item("P001", "Chocolate Cake", 2)))));

        AnalyticsSummaryResponse result = analyticsService.getSummary(null);

        assertEquals(0, result.totalOrders());
        assertEquals(new BigDecimal("0.00"), result.revenue());
        assertEquals(new BigDecimal("0.00"), result.averageOrderValue());
        assertTrue(result.topSellingProducts().isEmpty());
    }

    @Test
    void limitsTopProductsToFiveAndOrdersTiesByProductId() {
        Order order = order("o1", "store1", "CONFIRMED", "60.00", List.of(
                item("P006", "Product 6", 1),
                item("P003", "Product 3", 1),
                item("P001", "Product 1", 1),
                item("P005", "Product 5", 1),
                item("P002", "Product 2", 1),
                item("P004", "Product 4", 1)));
        when(orderRepository.findAll()).thenReturn(List.of(order));

        AnalyticsSummaryResponse result = analyticsService.getSummary(null);

        assertEquals(List.of("P001", "P002", "P003", "P004", "P005"),
                result.topSellingProducts().stream()
                        .map(TopSellingProductResponse::productId)
                        .toList());
    }

    @Test
    void recalculatesFromRepositoryOnEveryRequest() {
        Order firstOrder = order("o1", "store1", "CONFIRMED", "10.00",
                List.of(item("P001", "Chocolate Cake", 1)));
        Order secondOrder = order("o2", "store1", "CONFIRMED", "20.00",
                List.of(item("P001", "Chocolate Cake", 2)));
        when(orderRepository.findAll())
                .thenReturn(List.of(firstOrder))
                .thenReturn(List.of(firstOrder, secondOrder));

        AnalyticsSummaryResponse firstResult = analyticsService.getSummary(null);
        AnalyticsSummaryResponse secondResult = analyticsService.getSummary(null);

        assertEquals(1, firstResult.totalOrders());
        assertEquals(new BigDecimal("10.00"), firstResult.revenue());
        assertEquals(2, secondResult.totalOrders());
        assertEquals(new BigDecimal("30.00"), secondResult.revenue());
        assertEquals(3, secondResult.topSellingProducts().getFirst().quantitySold());
        verify(orderRepository, times(2)).findAll();
    }

    @Test
    void rejectsBlankStoreIdBeforeReadingOrders() {
        assertThrows(InvalidAnalyticsRequestException.class,
                () -> analyticsService.getSummary("  "));
    }

    private Order order(String id, String storeId, String status, String total,
                        List<OrderItem> items) {
        return new Order(id, "customer1", storeId, "TAKEOUT", BigDecimal.ZERO,
                null, new com.frostedcorner.orders.OrderCustomer("Alex", "alex@example.com", "555"),
                null, status, items, new BigDecimal(total),
                Instant.parse("2026-09-22T10:30:00Z"));
    }

    private OrderItem item(String productId, String productName, int quantity) {
        return new OrderItem(productId, productName, BigDecimal.ONE, quantity,
                BigDecimal.ONE.multiply(BigDecimal.valueOf(quantity)));
    }
}