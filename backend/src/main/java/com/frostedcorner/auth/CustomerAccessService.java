package com.frostedcorner.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerAccessService {

    private final UserRepository userRepository;

    public CustomerAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requireCustomerAccess(String requestedCustomerId) {
        User user = currentUser();
        if (user.getRole() == Role.CUSTOMER
                && requestedCustomerId != null
                && requestedCustomerId.equals(user.getCustomerId())) {
            return;
        }
        throw new AccessDeniedException("Customers may access only their own profile and order history.");
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("The signed-in user no longer exists."));
    }
}
