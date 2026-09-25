package com.frostedcorner.customers;

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
class CustomerDataSeederTest {

    @Mock
    private CustomerRepository customerRepository;

    @Test
    void seedsCompleteDemoCustomerProfile() throws Exception {
        CustomerDataSeeder seeder = new CustomerDataSeeder(customerRepository);

        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<Customer> customer = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customer.capture());

        Customer saved = customer.getValue();
        assertEquals("c1", saved.getId());
        assertEquals("Alex Carter", saved.getName());
        assertEquals("customer@frostedcorner.demo", saved.getEmail());
        assertEquals("555-0100", saved.getPhone());
        assertEquals("10001", saved.getZipCode());
        assertEquals(List.of("chocolate", "celebrations"), saved.getPreferences());
        assertEquals(List.of("Cupcakes", "Cookies"), saved.getFavoriteCategories());
    }
}
