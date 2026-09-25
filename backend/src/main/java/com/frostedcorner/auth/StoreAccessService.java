package com.frostedcorner.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StoreAccessService {

    private final UserRepository userRepository;

    public StoreAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requireAnalyticsAccess(String requestedStoreId) {
        User user = currentUser();
        if (user.getRole() == Role.OWNER) {
            return;
        }
        if (user.getRole() == Role.MANAGER) {
            requireAssignedStore(user, requestedStoreId, "Managers may view analytics only for their assigned store.");
            return;
        }
        throw new AccessDeniedException("Only managers and owners may view analytics.");
    }

    public void requireInventoryAccess(String requestedStoreId) {
        User user = currentUser();
        if (user.getRole() == Role.EMPLOYEE || user.getRole() == Role.MANAGER) {
            requireAssignedStore(user, requestedStoreId,
                    "Staff may access inventory only for their assigned store.");
            return;
        }
        throw new AccessDeniedException("Only employees and managers may access inventory.");
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("The signed-in user no longer exists."));
    }

    private void requireAssignedStore(User user, String requestedStoreId, String message) {
        if (requestedStoreId == null || !requestedStoreId.equals(user.getStoreId())) {
            throw new AccessDeniedException(message);
        }
    }
}