package com.frostedcorner.customers;

import com.frostedcorner.catalog.Product;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Customer getProfile(@PathVariable String customerId) {
        return customerService.getProfile(customerId);
    }

    @GetMapping("/orders")
    public CustomerOrderHistoryResponse getOrderHistory(@PathVariable String customerId) {
        return customerService.getOrderHistory(customerId);
    }

    @GetMapping("/{customerId}/recommendations")
    public List<Product> getRecommendations(@PathVariable String customerId) {
        return customerService.getRecommendations(customerId);
    }
}
