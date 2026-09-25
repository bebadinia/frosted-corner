package com.frostedcorner.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class OrderDataSeederTest {

    @Mock
    private OrderRepository orderRepository;

    @Test
    void seedsThreeOrdersForTheDemoCustomerAndManagerStore() throws Exception {
        OrderDataSeeder seeder = new OrderDataSeeder(orderRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Order>> orders = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(orders.capture());

        List<Order> savedOrders = orders.getValue();
        assertEquals(3, savedOrders.size());
        assertEquals(1, savedOrders.stream().map(Order::getCustomerId).distinct().count());
        assertEquals("c1", savedOrders.getFirst().getCustomerId());
        assertEquals(1, savedOrders.stream().map(Order::getStoreId).distinct().count());
        assertEquals("store1", savedOrders.getFirst().getStoreId());

        long cupcakeQuantity = savedOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .filter(item -> "P005".equals(item.getProductId()))
                .mapToLong(OrderItem::getQuantity)
                .sum();
        assertEquals(9, cupcakeQuantity);
    }
}
