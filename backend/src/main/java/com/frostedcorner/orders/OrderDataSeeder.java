package com.frostedcorner.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(5)
public class OrderDataSeeder implements ApplicationRunner {

    private final OrderRepository orderRepository;

    public OrderDataSeeder(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoOrders().forEach(orderRepository::save);
    }

    private List<Order> demoOrders() {
        OrderCustomer customer = new OrderCustomer(
                "Alex Carter", "customer@frostedcorner.demo", "555-0100");

        return List.of(
                new Order(
                        "demo-order-001", "c1", "store1", "TAKEOUT",
                        BigDecimal.ZERO, null, customer, null, "CONFIRMED",
                        List.of(new OrderItem(
                                "P005", "Chocolate Fudge Cupcake",
                                new BigDecimal("4.49"), 4, new BigDecimal("17.96"))),
                        new BigDecimal("17.96"),
                        Instant.parse("2026-09-03T18:30:00Z")),
                new Order(
                        "demo-order-002", "c1", "store1", "TAKEOUT",
                        BigDecimal.ZERO, null, customer, null, "DELIVERED",
                        List.of(
                                new OrderItem(
                                        "P008", "Chocolate Chip Cookie",
                                        new BigDecimal("2.99"), 6, new BigDecimal("17.94")),
                                new OrderItem(
                                        "P005", "Chocolate Fudge Cupcake",
                                        new BigDecimal("4.49"), 2, new BigDecimal("8.98"))),
                        new BigDecimal("26.92"),
                        Instant.parse("2026-09-12T16:15:00Z")),
                new Order(
                        "demo-order-003", "c1", "store1", "TAKEOUT",
                        BigDecimal.ZERO, null, customer, null, "CONFIRMED",
                        List.of(
                                new OrderItem(
                                        "P005", "Chocolate Fudge Cupcake",
                                        new BigDecimal("4.49"), 3, new BigDecimal("13.47")),
                                new OrderItem(
                                        "P011", "Classic Fudge Brownie",
                                        new BigDecimal("4.49"), 2, new BigDecimal("8.98"))),
                        new BigDecimal("22.45"),
                        Instant.parse("2026-09-20T19:45:00Z"))
        );
    }
}
