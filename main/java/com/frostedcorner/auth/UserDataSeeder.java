package com.frostedcorner.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class UserDataSeeder implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "demo-password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<User> users = List.of(
            user("user-customer", "customer@frostedcorner.demo", Role.CUSTOMER, "c1", null),
                user("user-employee", "employee@frostedcorner.demo", Role.EMPLOYEE, "store1"),
                user("user-manager", "manager@frostedcorner.demo", Role.MANAGER, "store1"),
                user("user-owner", "owner@frostedcorner.demo", Role.OWNER, null));
        users.forEach(this::saveIfMissingOrRepairCustomerLink);
    }

    private void saveIfMissingOrRepairCustomerLink(User seededUser) {
        Optional<User> existing = userRepository.findByEmail(seededUser.getEmail());
        if (existing.isEmpty()) {
            userRepository.save(seededUser);
            return;
        }

        User existingUser = existing.get();
        if (existingUser.getRole() == Role.CUSTOMER && existingUser.getCustomerId() == null) {
            userRepository.save(new User(existingUser.getId(), existingUser.getEmail(),
                    existingUser.getPasswordHash(), Role.CUSTOMER, "c1", null));
        }
    }

    private User user(String id, String email, Role role, String customerId, String storeId) {
        return new User(id, email, passwordEncoder.encode(DEMO_PASSWORD), role, customerId, storeId);
    }

    private User user(String id, String email, Role role, String storeId) {
        return user(id, email, role, null, storeId);
    }
}