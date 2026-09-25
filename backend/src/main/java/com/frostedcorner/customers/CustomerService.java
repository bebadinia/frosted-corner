package com.frostedcorner.customers;

import com.frostedcorner.auth.CustomerAccessService;
import com.frostedcorner.orders.Order;
import com.frostedcorner.orders.OrderItem;
import com.frostedcorner.orders.OrderRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private static final int FAVORITE_ITEM_LIMIT = 3;

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CustomerAccessService customerAccessService;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository,
                           CustomerAccessService customerAccessService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.customerAccessService = customerAccessService;
    }

    public Customer getProfile(String customerId) {
        customerAccessService.requireCustomerAccess(customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer profile not found."));
    }

    public CustomerOrderHistoryResponse getOrderHistory(String customerId) {
        customerAccessService.requireCustomerAccess(customerId);
        List<Order> orders = orderRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId);
        return new CustomerOrderHistoryResponse(orders, favoriteItems(orders));
    }

    private List<FavoriteItemResponse> favoriteItems(List<Order> orders) {
        Map<String, FavoriteItemAccumulator> totals = new HashMap<>();

        orders.stream()
                .flatMap(order -> order.getItems().stream())
                .forEach(item -> totals.compute(item.getProductId(),
                        (productId, current) -> aggregate(current, item)));

        return totals.values().stream()
                .sorted(Comparator.comparingLong(FavoriteItemAccumulator::quantityOrdered).reversed()
                        .thenComparing(FavoriteItemAccumulator::productId))
                .limit(FAVORITE_ITEM_LIMIT)
                .map(item -> new FavoriteItemResponse(
                        item.productId(), item.productName(), item.quantityOrdered()))
                .toList();
    }

    private FavoriteItemAccumulator aggregate(FavoriteItemAccumulator current, OrderItem item) {
        if (current == null) {
            return new FavoriteItemAccumulator(
                    item.getProductId(), item.getProductName(), item.getQuantity());
        }
        return new FavoriteItemAccumulator(
                current.productId(), current.productName(),
                current.quantityOrdered() + item.getQuantity());
    }

    private record FavoriteItemAccumulator(
            String productId,
            String productName,
            long quantityOrdered) {
    }
}
