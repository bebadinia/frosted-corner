package com.frostedcorner.orders;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class LocalDeliveryProviderAssigner {

    private static final List<String> PROVIDERS = List.of("DoorDash", "Uber Eats");

    public String assignProvider() {
        return PROVIDERS.get(ThreadLocalRandom.current().nextInt(PROVIDERS.size()));
    }
}