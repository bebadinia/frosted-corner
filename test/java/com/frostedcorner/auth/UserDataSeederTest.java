package com.frostedcorner.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
    void seedsOneHashedDemoAccountForEachRole() throws Exception {
        when(userRepository.findByEmail(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        UserDataSeeder seeder = new UserDataSeeder(userRepository, new BCryptPasswordEncoder());

        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<User> users = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.times(4)).save(users.capture());
        assertEquals(java.util.Set.of(Role.CUSTOMER, Role.EMPLOYEE, Role.MANAGER, Role.OWNER),
                users.getAllValues().stream().map(User::getRole).collect(java.util.stream.Collectors.toSet()));
        assertFalse(users.getAllValues().stream().anyMatch(
                user -> "demo-password".equals(user.getPasswordHash())));
        assertEquals("store1", users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE).findFirst().orElseThrow().getStoreId());
        assertEquals("store1", users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.MANAGER).findFirst().orElseThrow().getStoreId());
        assertEquals("c1", users.getAllValues().stream()
                .filter(user -> user.getRole() == Role.CUSTOMER).findFirst().orElseThrow().getCustomerId());
    }

    @Test
    void requiresAStoreForEmployeeAndManagerRolesOnly() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User("employee", "employee@example.com", "hash", Role.EMPLOYEE, null));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User("manager", "manager@example.com", "hash", Role.MANAGER, " "));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User("owner", "owner@example.com", "hash", Role.OWNER, "store1"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User("customer", "customer@example.com", "hash", Role.CUSTOMER, null));
    }

    @Test
        void repairsTheExistingDemoCustomerUserWhenItLacksCustomerLink() throws Exception {
                User legacyCustomer = org.mockito.Mockito.mock(User.class);
                when(legacyCustomer.getId()).thenReturn("user-customer");
                when(legacyCustomer.getEmail()).thenReturn("customer@frostedcorner.demo");
                when(legacyCustomer.getPasswordHash()).thenReturn("hash");
                when(legacyCustomer.getRole()).thenReturn(Role.CUSTOMER);
                when(legacyCustomer.getCustomerId()).thenReturn(null);
        when(userRepository.findByEmail("customer@frostedcorner.demo"))
                .thenReturn(Optional.of(legacyCustomer));
        when(userRepository.findByEmail(org.mockito.ArgumentMatchers.argThat(
                email -> !"customer@frostedcorner.demo".equals(email))))
                .thenReturn(Optional.of(new User("existing", "existing@example.com", "hash",
                        Role.OWNER, null)));
        UserDataSeeder seeder = new UserDataSeeder(userRepository, new BCryptPasswordEncoder());

        seeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(user.capture());
        assertEquals("c1", user.getValue().getCustomerId());
        assertEquals("hash", user.getValue().getPasswordHash());
    }
}