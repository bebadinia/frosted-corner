package com.frostedcorner.locations;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class FranchiseLocationDataSeeder implements ApplicationRunner {

    private static final List<FranchiseLocation> DEMO_LOCATIONS = List.of(
            location(1, "New York", "NY", "10001", "101 Broadway", "Avery Morgan", 40.7128, -74.0060),
            location(2, "Los Angeles", "CA", "90012", "220 Spring Street", "Jordan Lee", 34.0522, -118.2437),
            location(3, "Chicago", "IL", "60601", "315 Michigan Avenue", "Taylor Brooks", 41.8781, -87.6298),
            location(4, "Houston", "TX", "77002", "410 Main Street", "Cameron Diaz", 29.7604, -95.3698),
            location(5, "Phoenix", "AZ", "85004", "525 Central Avenue", "Riley Carter", 33.4484, -112.0740),
            location(6, "Philadelphia", "PA", "19107", "630 Market Street", "Morgan Reed", 39.9526, -75.1652),
            location(7, "San Antonio", "TX", "78205", "745 Commerce Street", "Casey Bennett", 29.4241, -98.4936),
            location(8, "San Diego", "CA", "92101", "850 Broadway", "Quinn Foster", 32.7157, -117.1611),
            location(9, "Dallas", "TX", "75201", "915 Elm Street", "Parker Hayes", 32.7767, -96.7970),
            location(10, "Jacksonville", "FL", "32202", "1020 Bay Street", "Reese Collins", 30.3322, -81.6557),
            location(11, "Austin", "TX", "78701", "1125 Congress Avenue", "Skyler Ward", 30.2672, -97.7431),
            location(12, "Fort Worth", "TX", "76102", "1230 Houston Street", "Drew Parker", 32.7555, -97.3308),
            location(13, "San Jose", "CA", "95113", "1335 Santa Clara Street", "Alexis Turner", 37.3382, -121.8863),
            location(14, "Columbus", "OH", "43215", "1440 High Street", "Emerson Scott", 39.9612, -82.9988),
            location(15, "Charlotte", "NC", "28202", "1545 Tryon Street", "Rowan Kelly", 35.2271, -80.8431),
            location(16, "Indianapolis", "IN", "46204", "1650 Meridian Street", "Finley Price", 39.7684, -86.1581),
            location(17, "Seattle", "WA", "98101", "1755 Pine Street", "Harper Adams", 47.6062, -122.3321),
            location(18, "Denver", "CO", "80202", "1860 Larimer Street", "Dakota Hughes", 39.7392, -104.9903),
            location(19, "Washington", "DC", "20001", "1965 K Street NW", "Sydney Ross", 38.9072, -77.0369),
            location(20, "Nashville", "TN", "37203", "2070 Broadway", "Blake Cooper", 36.1627, -86.7816),
            location(21, "Oklahoma City", "OK", "73102", "2175 Sheridan Avenue", "Kendall Bailey", 35.4676, -97.5164),
            location(22, "El Paso", "TX", "79901", "2280 Mesa Street", "Jamie Rivera", 31.7619, -106.4850),
            location(23, "Boston", "MA", "02108", "2385 Tremont Street", "Logan Murphy", 42.3601, -71.0589),
            location(24, "Portland", "OR", "97205", "2490 Burnside Street", "Sage Peterson", 45.5152, -122.6784),
            location(25, "Las Vegas", "NV", "89101", "2595 Fremont Street", "Charlie Ramirez", 36.1699, -115.1398),
            location(26, "Detroit", "MI", "48226", "2700 Woodward Avenue", "Robin Sanders", 42.3314, -83.0458),
            location(27, "Memphis", "TN", "38103", "2805 Union Avenue", "Ari Simmons", 35.1495, -90.0490),
            location(28, "Louisville", "KY", "40202", "2910 Main Street", "Hayden Powell", 38.2527, -85.7585),
            location(29, "Baltimore", "MD", "21201", "3015 Charles Street", "Marley Long", 39.2904, -76.6122),
            location(30, "Milwaukee", "WI", "53202", "3120 Wisconsin Avenue", "Corey Patterson", 43.0389, -87.9065),
            location(31, "Albuquerque", "NM", "87102", "3225 Central Avenue", "Jules Richardson", 35.0844, -106.6504),
            location(32, "Tucson", "AZ", "85701", "3330 Congress Street", "Micah Cox", 32.2226, -110.9747),
            location(33, "Fresno", "CA", "93721", "3435 Fulton Street", "Lane Howard", 36.7378, -119.7871),
            location(34, "Sacramento", "CA", "95814", "3540 Capitol Mall", "Shawn Bryant", 38.5816, -121.4944),
            location(35, "Kansas City", "MO", "64106", "3645 Grand Boulevard", "Devon Griffin", 39.0997, -94.5786),
            location(36, "Atlanta", "GA", "30303", "3750 Peachtree Street", "Ellis Russell", 33.7490, -84.3880),
            location(37, "Miami", "FL", "33130", "3855 Biscayne Boulevard", "Remy Jenkins", 25.7617, -80.1918),
            location(38, "Raleigh", "NC", "27601", "3960 Fayetteville Street", "Noel Perry", 35.7796, -78.6382),
            location(39, "Omaha", "NE", "68102", "4065 Farnam Street", "Frankie Butler", 41.2565, -95.9345),
            location(40, "Minneapolis", "MN", "55401", "4170 Hennepin Avenue", "Kerry Barnes", 44.9778, -93.2650)
        );

    private final FranchiseLocationRepository locationRepository;

    public FranchiseLocationDataSeeder(FranchiseLocationRepository locationRepository) 
    {
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<FranchiseLocation> locationsToSeed = DEMO_LOCATIONS.stream()
                .filter(this::shouldSeed)
                .toList();

        if (!locationsToSeed.isEmpty()) {
            locationRepository.saveAll(locationsToSeed);
        }
    }

    private boolean shouldSeed(FranchiseLocation seededLocation) {
        Optional<FranchiseLocation> existingLocation = locationRepository.findById(seededLocation.getId());
        return existingLocation.isEmpty() || !matchesSeed(existingLocation.get(), seededLocation);
    }

    private boolean matchesSeed(FranchiseLocation existingLocation, FranchiseLocation seededLocation) {
        return existingLocation.getStoreName().equals(seededLocation.getStoreName())
                && existingLocation.getStreet().equals(seededLocation.getStreet())
                && existingLocation.getCity().equals(seededLocation.getCity())
                && existingLocation.getState().equals(seededLocation.getState())
                && existingLocation.getZipCode().equals(seededLocation.getZipCode())
                && existingLocation.getManagerName().equals(seededLocation.getManagerName())
                && Double.compare(existingLocation.getLatitude(), seededLocation.getLatitude()) == 0
                && Double.compare(existingLocation.getLongitude(), seededLocation.getLongitude()) == 0;
    }

    private static FranchiseLocation location(int number, String city, String state,
                                              String zipCode, String street, String managerName,
                                              double latitude, double longitude) {
        return new FranchiseLocation("store" + number,
                "Frosted Corner - " + city,
                street,
                city,
                state,
                zipCode,
                managerName,
                latitude,
                longitude);
    }
}