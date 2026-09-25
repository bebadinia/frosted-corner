package com.frostedcorner.customers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frostedcorner.auth.CustomerAccessService;
import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductService;
import com.frostedcorner.orders.Order;
import com.frostedcorner.orders.OrderCustomer;
import com.frostedcorner.orders.OrderItem;
import com.frostedcorner.orders.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerAccessService customerAccessService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void returnsTheSignedInCustomersProfile() {
        Customer customer = new Customer("c1", "Alex Carter", "customer@frostedcorner.demo");
        when(customerRepository.findById("c1")).thenReturn(Optional.of(customer));

        Customer result = customerService.getProfile("c1");

        verify(customerAccessService).requireCustomerAccess("c1");
        assertEquals("Alex Carter", result.getName());
    }

    @Test
    void returnsOrdersAndRanksFavoriteItemsByQuantity() {
        when(orderRepository.findAllByCustomerIdOrderByCreatedAtDesc("c1"))
                .thenReturn(List.of(
                        order("o2", List.of(
                                item("P005", "Chocolate Fudge Cupcake", 2),
                                item("P008", "Chocolate Chip Cookie", 6))),
                        order("o1", List.of(
                                item("P005", "Chocolate Fudge Cupcake", 4),
                                item("P011", "Classic Fudge Brownie", 2)))));

        CustomerOrderHistoryResponse result = customerService.getOrderHistory("c1");

        verify(customerAccessService).requireCustomerAccess("c1");
        assertEquals(2, result.orders().size());
        assertEquals(List.of(
                new FavoriteItemResponse("P005", "Chocolate Fudge Cupcake", 6),
                new FavoriteItemResponse("P008", "Chocolate Chip Cookie", 6),
                new FavoriteItemResponse("P011", "Classic Fudge Brownie", 2)),
                result.favoriteItems());
    }

    @Test
    void recommendsTopPreviouslyOrderedActiveProducts() {
        when(orderRepository.findAllByCustomerIdOrderByCreatedAtDesc("c1"))
                .thenReturn(List.of(
                        order("o2", List.of(
                                item("P005", "Chocolate Fudge Cupcake", 2),
                                item("P008", "Chocolate Chip Cookie", 6))),
                        order("o1", List.of(
                                item("P005", "Chocolate Fudge Cupcake", 4),
                                item("P011", "Classic Fudge Brownie", 2)))));
        Product cupcake = new Product(
                "P005", "Chocolate Fudge Cupcake", "Chocolate cupcake",
                new BigDecimal("4.49"), "Cupcakes", "cupcake.jpg", true);
        Product cookie = new Product(
                "P008", "Chocolate Chip Cookie", "Chocolate chip cookie",
                new BigDecimal("2.99"), "Cookies", "cookie.jpg", true);
        Product brownie = new Product(
                "P011", "Classic Fudge Brownie", "Fudge brownie",
                new BigDecimal("4.49"), "Brownies", "brownie.jpg", true);
        when(productService.getActiveProducts()).thenReturn(List.of(cupcake, cookie, brownie));

        List<Product> recommendations = customerService.getRecommendations("c1");

        verify(customerAccessService).requireCustomerAccess("c1");
        assertEquals(List.of("P005", "P008", "P011"),
                recommendations.stream().map(Product::getId).toList());
    }

    private Order order(String id, List<OrderItem> items) {
        return new Order(
                id, "c1", "store1", "TAKEOUT", BigDecimal.ZERO, null,
                new OrderCustomer("Alex Carter", "customer@frostedcorner.demo", "555-0100"),
                null, "CONFIRMED", items, new BigDecimal("20.00"),
                Instant.parse("2026-09-20T19:45:00Z"));
    }

    private OrderItem item(String productId, String productName, int quantity) {
        return new OrderItem(
                productId, productName, BigDecimal.ONE, quantity,
                BigDecimal.valueOf(quantity));
    }
}
