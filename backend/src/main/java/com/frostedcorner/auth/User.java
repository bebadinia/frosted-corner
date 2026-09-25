package com.frostedcorner.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private Role role;
    private String customerId;
    private String storeId;

    public User() {
    }

    public User(String id, String email, String passwordHash, Role role, String storeId) {
        this(id, email, passwordHash, role, null, storeId);
    }

    public User(String id, String email, String passwordHash, Role role, String customerId,
                String storeId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customerId = normalizeIdentifier(customerId);
        this.storeId = normalizeStoreId(storeId);
        validateRoleAssignments();
    }

    private String normalizeStoreId(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeIdentifier(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateRoleAssignments() {
        if (role == null) {
            throw new IllegalArgumentException("A user role is required.");
        }
        if ((role == Role.EMPLOYEE || role == Role.MANAGER) && storeId == null) {
            throw new IllegalArgumentException(role + " users require an assigned store.");
        }
        if ((role == Role.CUSTOMER || role == Role.OWNER) && storeId != null) {
            throw new IllegalArgumentException(role + " users must not have an assigned store.");
        }
        if (role == Role.CUSTOMER && customerId == null) {
            throw new IllegalArgumentException("CUSTOMER users require a linked customer profile.");
        }
        if (role != Role.CUSTOMER && customerId != null) {
            throw new IllegalArgumentException(role + " users must not have a linked customer profile.");
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStoreId() {
        return storeId;
    }
}