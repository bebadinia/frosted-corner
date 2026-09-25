package com.frostedcorner.customers;

import java.util.List;
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
        Customer customer = new Customer(
                "c1", "Alex Carter", "customer@frostedcorner.demo");
        customer.setPhone("555-0100");
        customer.setStreet("101 Broadway");
        customer.setCity("New York");
        customer.setState("NY");
        customer.setZipCode("10001");
        customer.setPreferences(List.of("chocolate", "celebrations"));
        customer.setFavoriteCategories(List.of("Cupcakes", "Cookies"));
        customerRepository.save(customer);
    }
}
