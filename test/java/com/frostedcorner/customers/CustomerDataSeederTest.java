package com.frostedcorner.customers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void seedsTheCustomerProfileLinkedToTheDemoCustomerUser() throws Exception {
        when(customerRepository.existsById("c1")).thenReturn(false);
        CustomerDataSeeder seeder = new CustomerDataSeeder(customerRepository);

        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<Customer> customer = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customer.capture());
        assertEquals("c1", customer.getValue().getId());
        assertEquals("customer@frostedcorner.demo", customer.getValue().getEmail());
    }
}