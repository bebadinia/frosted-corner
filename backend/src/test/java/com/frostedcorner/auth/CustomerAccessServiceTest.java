package com.frostedcorner.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CustomerAccessServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerAccessService customerAccessService;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCanAccessOnlyTheirLinkedProfile() {
        signIn(new User(
                "customer-user",
                "customer@frostedcorner.demo",
                "hash",
                Role.CUSTOMER,
                "c1",
                null));

        assertDoesNotThrow(() -> customerAccessService.requireCustomerAccess("c1"));
        assertThrows(AccessDeniedException.class,
                () -> customerAccessService.requireCustomerAccess("c2"));
    }

    @Test
    void managerCannotUseCustomerSelfServicePages() {
        signIn(new User(
                "manager-user",
                "manager@frostedcorner.demo",
                "hash",
                Role.MANAGER,
                null,
                "store1"));

        assertThrows(AccessDeniedException.class,
                () -> customerAccessService.requireCustomerAccess("c1"));
    }

    private void signIn(User user) {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getEmail(), "N/A", java.util.List.of()));
    }
}
