package com.frostedcorner.auth;

public record AuthenticatedUserResponse(String id, String email, Role role, String customerId,
                                        String storeId) {

    static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.getId(), user.getEmail(), user.getRole(),
                user.getCustomerId(), user.getStoreId());
    }
}