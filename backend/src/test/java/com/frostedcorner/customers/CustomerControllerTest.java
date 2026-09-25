package com.frostedcorner.customers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frostedcorner.catalog.Product;
import com.frostedcorner.orders.Order;
import com.frostedcorner.orders.OrderCustomer;
import com.frostedcorner.orders.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void returnsCustomerProfile() throws Exception {
        Customer customer = new Customer("c1", "Alex Carter", "customer@frostedcorner.demo");
        customer.setPhone("555-0100");
        customer.setCity("New York");
        customer.setState("NY");
        customer.setZipCode("10001");

        when(customerService.getProfile("c1")).thenReturn(customer);

        mockMvc.perform(get("/api/customers/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"))
                .andExpect(jsonPath("$.name").value("Alex Carter"))
                .andExpect(jsonPath("$.zipCode").value("10001"));
    }

    @Test
    void returnsHistoryBasedRecommendations() throws Exception {
        when(customerService.getRecommendations("c1"))
                .thenReturn(List.of(new Product(
                        "P005", "Chocolate Fudge Cupcake", "Chocolate cupcake",
                        new BigDecimal("4.49"), "Cupcakes", "cupcake.jpg", true)));

        mockMvc.perform(get("/api/customers/c1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("P005"))
                .andExpect(jsonPath("$[0].name").value("Chocolate Fudge Cupcake"));
    }

    @Test
    void returnsOrdersAndFavoriteItems() throws Exception {
        Order order = new Order(
                "demo-order-001", "c1", "store1", "TAKEOUT",
                BigDecimal.ZERO, null,
                new OrderCustomer("Alex Carter", "customer@frostedcorner.demo", "555-0100"),
                null, "CONFIRMED",
                List.of(new OrderItem(
                        "P005", "Chocolate Fudge Cupcake",
                        new BigDecimal("4.49"), 4, new BigDecimal("17.96"))),
                new BigDecimal("17.96"),
                Instant.parse("2026-09-03T18:30:00Z"));

        when(customerService.getOrderHistory("c1"))
                .thenReturn(new CustomerOrderHistoryResponse(
                        List.of(order),
                        List.of(new FavoriteItemResponse(
                                "P005", "Chocolate Fudge Cupcake", 4))));

        mockMvc.perform(get("/api/customers/c1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value("demo-order-001"))
                .andExpect(jsonPath("$.favoriteItems[0].productId").value("P005"))
                .andExpect(jsonPath("$.favoriteItems[0].quantityOrdered").value(4));
    }
}
