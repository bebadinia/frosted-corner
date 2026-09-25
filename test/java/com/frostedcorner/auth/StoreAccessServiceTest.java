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
class StoreAccessServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StoreAccessService storeAccessService;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void managerCanViewAnalyticsOnlyForAssignedStore() {
        signIn(new User("manager", "manager@frostedcorner.demo", "hash", Role.MANAGER, "store1"));

        assertDoesNotThrow(() -> storeAccessService.requireAnalyticsAccess("store1"));
        assertThrows(AccessDeniedException.class,
                () -> storeAccessService.requireAnalyticsAccess("store2"));
        assertThrows(AccessDeniedException.class,
                () -> storeAccessService.requireAnalyticsAccess(null));
    }

    @Test
    void employeeCanAccessOnlyAssignedInventoryStore() {
        signIn(new User("employee", "employee@frostedcorner.demo", "hash", Role.EMPLOYEE, "store1"));

        assertDoesNotThrow(() -> storeAccessService.requireInventoryAccess("store1"));
        assertThrows(AccessDeniedException.class,
                () -> storeAccessService.requireInventoryAccess("store2"));
        assertThrows(AccessDeniedException.class,
                () -> storeAccessService.requireAnalyticsAccess("store1"));
    }

    @Test
    void ownerCanViewAllStoreAnalyticsButCannotUseInventoryEndpoint() {
        signIn(new User("owner", "owner@frostedcorner.demo", "hash", Role.OWNER, null));

        assertDoesNotThrow(() -> storeAccessService.requireAnalyticsAccess(null));
        assertThrows(AccessDeniedException.class,
                () -> storeAccessService.requireInventoryAccess("store1"));
    }

    private void signIn(User user) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user.getEmail(), "N/A", java.util.List.of()));
    }
}