package com.frostedcorner.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import com.frostedcorner.customers.Customer;
import com.frostedcorner.customers.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContextRepository securityContextRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsAndSavesAUserSessionAfterSuccessfulLogin() {
        User manager = new User("manager", "manager@frostedcorner.demo", "bcrypt-hash",
                Role.MANAGER, "store1");
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                manager.getEmail(), "N/A", java.util.List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));

        AuthenticatedUserResponse result = authService.login(
                new LoginRequest(manager.getEmail(), "demo-password"), servletRequest, servletResponse);

        assertEquals("manager", result.id());
        assertEquals(Role.MANAGER, result.role());
        assertEquals("store1", result.storeId());
        ArgumentCaptor<SecurityContext> context = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(context.capture(), eq(servletRequest),
            eq(servletResponse));
        assertEquals(manager.getEmail(), context.getValue().getAuthentication().getName());
    }

    @Test
    void rejectsInvalidLoginCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("manager@frostedcorner.demo", "wrong"),
                        servletRequest, servletResponse));

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void invalidatesTheExistingSessionOnLogout() {
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        when(servletRequest.getSession(false)).thenReturn(session);

        authService.logout(servletRequest);

        verify(session).invalidate();
    }

    @Test
    void registersCustomerUserWithALinkedCustomerProfileAndHashedPassword() {
        when(userRepository.findByEmail("new.customer@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("safe-password")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticatedUserResponse result = authService.register(
                new RegisterRequest("New Customer", " New.Customer@Example.com ", "safe-password"));

        ArgumentCaptor<Customer> customer = ArgumentCaptor.forClass(Customer.class);
        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(customerRepository).save(customer.capture());
        verify(userRepository).save(user.capture());
        assertEquals(Role.CUSTOMER, user.getValue().getRole());
        assertEquals(customer.getValue().getId(), user.getValue().getCustomerId());
        assertEquals("bcrypt-hash", user.getValue().getPasswordHash());
        assertEquals(customer.getValue().getId(), result.customerId());
        assertEquals("new.customer@example.com", result.email());
    }

    @Test
    void rejectsRegistrationForAnExistingEmailWithoutCreatingCustomerData() {
        User existingUser = new User("existing", "customer@example.com", "hash", Role.CUSTOMER,
                "existing-customer", null);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.register(new RegisterRequest("Customer", "customer@example.com",
                        "safe-password")));

        assertEquals(409, exception.getStatusCode().value());
        org.mockito.Mockito.verifyNoInteractions(customerRepository);
    }
}