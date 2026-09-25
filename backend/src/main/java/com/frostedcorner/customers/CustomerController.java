package com.frostedcorner.customers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    public Customer getProfile(@PathVariable String customerId) {
        return customerService.getProfile(customerId);
    }

    @GetMapping("/{customerId}/orders")
    public CustomerOrderHistoryResponse getOrderHistory(@PathVariable String customerId) {
        return customerService.getOrderHistory(customerId);
    }
}
