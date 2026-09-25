package com.frostedcorner.locations;

import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DemoCustomerLocationResolver {

    private static final Map<String, DemoCustomerCoordinates> APPROVED_DEMO_LOCATIONS = Map.ofEntries(
        Map.entry("10001", new DemoCustomerCoordinates("10001", 40.7128, -74.0060)),
        Map.entry("101 broadway, new york, ny 10001",
            new DemoCustomerCoordinates("101 Broadway, New York, NY 10001", 40.7128, -74.0060)),
        Map.entry("60601", new DemoCustomerCoordinates("60601", 41.8781, -87.6298)),
        Map.entry("315 michigan avenue, chicago, il 60601",
            new DemoCustomerCoordinates("315 Michigan Avenue, Chicago, IL 60601", 41.8781, -87.6298)),
        Map.entry("97205", new DemoCustomerCoordinates("97205", 45.5152, -122.6784)),
        Map.entry("2490 burnside street, portland, or 97205",
            new DemoCustomerCoordinates("2490 Burnside Street, Portland, OR 97205", 45.5152, -122.6784)),
        Map.entry("97301", new DemoCustomerCoordinates("97301", 44.9429, -123.0351)),
        Map.entry("145 liberty street se, salem, or 97301",
            new DemoCustomerCoordinates("145 Liberty Street SE, Salem, OR 97301", 44.9429, -123.0351)),
        Map.entry("98101", new DemoCustomerCoordinates("98101", 47.6062, -122.3321)),
        Map.entry("1755 pine street, seattle, wa 98101",
            new DemoCustomerCoordinates("1755 Pine Street, Seattle, WA 98101", 47.6062, -122.3321)),
        Map.entry("33130", new DemoCustomerCoordinates("33130", 25.7617, -80.1918)),
        Map.entry("3855 biscayne boulevard, miami, fl 33130",
            new DemoCustomerCoordinates("3855 Biscayne Boulevard, Miami, FL 33130", 25.7617, -80.1918)));

    public DemoCustomerCoordinates resolve(String demoAddressOrZip) {
        String normalizedInput = normalize(demoAddressOrZip);
        DemoCustomerCoordinates coordinates = APPROVED_DEMO_LOCATIONS.get(normalizedInput);
        if (coordinates == null) {
            throw new UnsupportedDemoLocationException(demoAddressOrZip);
        }
        return coordinates;
    }

    private String normalize(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidLocationLookupException("demoAddressOrZip must not be blank");
        }
        return input.trim().toLowerCase(Locale.US).replaceAll("\\s+", " ");
    }
}