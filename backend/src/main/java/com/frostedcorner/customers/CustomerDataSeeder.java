package com.frostedcorner.customers;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class CustomerDataSeeder implements ApplicationRunner {

    private final CustomerRepository customerRepository;

    public CustomerDataSeeder(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!customerRepository.existsById("c1")) {
            customerRepository.save(new Customer("c1", "Demo Customer", "customer@frostedcorner.demo"));
        }
    }
}