package com.frostedcorner.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frostedcorner.inventory.InsufficientInventoryException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createsOrderUsingDocumentedResponseShape() throws Exception {
        Order order = new Order("o100", "c1", "store24", "LOCAL_DELIVERY",
                new BigDecimal("2.99"), "DoorDash",
                new OrderCustomer("Alex Carter", "alex@example.com", "555-0100"),
                new OrderDeliveryAddress("2490 Burnside Street", "Portland", "OR", "97205"),
                "CONFIRMED",
                List.of(new OrderItem("P001", "Chocolate Cake", new BigDecimal("32.99"),
                        2, new BigDecimal("65.98"))),
                new BigDecimal("68.97"), Instant.parse("2026-09-22T10:30:00Z"));
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "c1",
                                  "fulfillmentOption": "DELIVERY",
                                  "customer": {
                                    "name": "Alex Carter",
                                    "email": "alex@example.com",
                                    "phone": "555-0100",
                                    "street": "2490 Burnside Street",
                                    "city": "Portland",
                                    "state": "OR",
                                    "zipCode": "97205"
                                  },
                                  "total": 0.01,
                                  "items": [
                                    {"productId": "P001", "quantity": 2, "unitPrice": 0.01}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o100"))
                .andExpect(jsonPath("$.customerId").value("c1"))
                .andExpect(jsonPath("$.storeId").value("store24"))
                .andExpect(jsonPath("$.fulfillmentType").value("LOCAL_DELIVERY"))
                .andExpect(jsonPath("$.fulfillmentFee").value(2.99))
                .andExpect(jsonPath("$.fulfillmentProvider").value("DoorDash"))
                .andExpect(jsonPath("$.customer.name").value("Alex Carter"))
                .andExpect(jsonPath("$.deliveryAddress.street").value("2490 Burnside Street"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items[0].productId").value("P001"))
                .andExpect(jsonPath("$.items[0].productName").value("Chocolate Cake"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(32.99))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(65.98))
                .andExpect(jsonPath("$.total").value(68.97))
                .andExpect(jsonPath("$.createdAt").value("2026-09-22T10:30:00Z"));
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void returnsOrderQuoteWithoutCreatingOrder() throws Exception {
        OrderQuoteResponse quote = new OrderQuoteResponse(
                "SHIPPING",
                "store17",
                new BigDecimal("31.99"),
                new BigDecimal("4.99"),
                new BigDecimal("0.00"),
                new BigDecimal("4.99"),
                true,
                new BigDecimal("31.99"));
        when(orderService.quoteOrder(any(CreateOrderRequest.class))).thenReturn(quote);

        mockMvc.perform(post("/api/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"c1",
                                  "fulfillmentOption":"DELIVERY",
                                  "customer":{
                                    "name":"Alex Carter",
                                    "email":"alex@example.com",
                                    "phone":"555-0100",
                                    "street":"101 Broadway",
                                    "city":"New York",
                                    "state":"NY",
                                    "zipCode":"10001"
                                  },
                                  "items":[{"productId":"P004","quantity":1}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fulfillmentType").value("SHIPPING"))
                .andExpect(jsonPath("$.standardFulfillmentFee").value(4.99))
                .andExpect(jsonPath("$.fulfillmentFee").value(0.00))
                .andExpect(jsonPath("$.promotionSavings").value(4.99))
                .andExpect(jsonPath("$.promotionApplied").value(true))
                .andExpect(jsonPath("$.estimatedTotal").value(31.99));
        verify(orderService).quoteOrder(any(CreateOrderRequest.class));
    }

    @Test
    void rejectsMissingCustomerId() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fulfillmentOption":"TAKEOUT","storeId":"store1","customer":{"name":"Alex","email":"alex@example.com","phone":"555"},"items":[{"productId":"P001","quantity":1}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyItems() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"customerId\":\"c1\"," +
                                "\"fulfillmentOption\":\"TAKEOUT\"," +
                                "\"storeId\":\"store1\"," +
                                "\"customer\":{\"name\":\"Alex\",\"email\":\"alex@example.com\",\"phone\":\"555\"}," +
                                "\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"c1",
                                  "fulfillmentOption":"TAKEOUT",
                                  "storeId":"store1",
                                  "customer":{"name":"Alex","email":"alex@example.com","phone":"555"},
                                  "items":[{"productId":"P001","quantity":0}]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new ProductNotFoundException("P999"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("P999", 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictForInsufficientInventory() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new InsufficientInventoryException("store1", "P001", 100, 20));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("P001", 100)))
                .andExpect(status().isConflict());
    }

    private String validRequest(String productId, int quantity) {
        return "{" +
                "\"customerId\":\"c1\"," +
                "\"fulfillmentOption\":\"TAKEOUT\"," +
                "\"storeId\":\"store1\"," +
                "\"customer\":{\"name\":\"Alex\",\"email\":\"alex@example.com\",\"phone\":\"555\"}," +
                "\"items\":[{\"productId\":\"" + productId + "\",\"quantity\":" + quantity + "}]}";
    }
}