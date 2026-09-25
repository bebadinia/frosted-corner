package com.frostedcorner.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void seedsCustomerAndManagerDemoAccounts() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        UserDataSeeder seeder = new UserDataSeeder(userRepository, new BCryptPasswordEncoder());

        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<User> users = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(users.capture());

        assertEquals(Set.of(Role.CUSTOMER, Role.MANAGER),
                users.getAllValues().stream().map(User::getRole).collect(Collectors.toSet()));
        assertFalse(users.getAllValues().stream().anyMatch(
                user -> "demo-password".equals(user.getPasswordHash())));
        assertEquals("store1", users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.MANAGER)
                .findFirst().orElseThrow().getStoreId());
        assertEquals("c1", users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .findFirst().orElseThrow().getCustomerId());
    }

    @Test
    void repairsExistingDemoAssignmentsWithoutChangingPasswordHash() throws Exception {
        User legacyCustomer = org.mockito.Mockito.mock(User.class);
        when(legacyCustomer.getId()).thenReturn("user-customer");
        when(legacyCustomer.getEmail()).thenReturn("customer@frostedcorner.demo");
        when(legacyCustomer.getPasswordHash()).thenReturn("existing-hash");
        when(legacyCustomer.getRole()).thenReturn(Role.CUSTOMER);
        when(legacyCustomer.getCustomerId()).thenReturn(null);
        when(legacyCustomer.getStoreId()).thenReturn(null);

        when(userRepository.findByEmail("customer@frostedcorner.demo"))
                .thenReturn(Optional.of(legacyCustomer));
        when(userRepository.findByEmail("manager@frostedcorner.demo"))
                .thenReturn(Optional.empty());

        UserDataSeeder seeder = new UserDataSeeder(userRepository, new BCryptPasswordEncoder());
        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<User> users = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(users.capture());

        User repairedCustomer = users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .findFirst().orElseThrow();
        assertEquals("c1", repairedCustomer.getCustomerId());
        assertEquals("existing-hash", repairedCustomer.getPasswordHash());
    }
}
