package com.frostedcorner.locations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class FranchiseLocationDataSeederTest {

    @Mock
    private FranchiseLocationRepository locationRepository;

    @Test
    void seedsFortyFranchiseLocations() throws Exception {
        when(locationRepository.findById(anyString())).thenReturn(Optional.empty());
        FranchiseLocationDataSeeder seeder = new FranchiseLocationDataSeeder(locationRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FranchiseLocation>> locationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(locationRepository).saveAll(locationsCaptor.capture());
        List<FranchiseLocation> locations = locationsCaptor.getValue();
        assertEquals(40, locations.size());
        assertEquals("store1", locations.getFirst().getId());
        assertEquals("New York", locations.getFirst().getCity());
        assertEquals(40.7128, locations.getFirst().getLatitude());
        assertEquals(-74.0060, locations.getFirst().getLongitude());
        assertEquals("store40", locations.getLast().getId());
        assertEquals("Minneapolis", locations.getLast().getCity());
        assertEquals(44.9778, locations.getLast().getLatitude());
        assertEquals(-93.2650, locations.getLast().getLongitude());
    }

    @Test
    void doesNotOverwriteExistingLocations() throws Exception {
        when(locationRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            int number = Integer.parseInt(id.replace("store", ""));
            return Optional.of(expectedLocation(number));
        });
        FranchiseLocationDataSeeder seeder = new FranchiseLocationDataSeeder(locationRepository);

        seeder.run(new DefaultApplicationArguments());

        verify(locationRepository, never()).saveAll(anyList());
        }

        @Test
        void updatesExistingLocationsWhenCoordinatesAreMissing() throws Exception {
        FranchiseLocation existing = new FranchiseLocation("store1", "Frosted Corner - New York",
            "101 Broadway", "New York", "NY", "10001", "Avery Morgan", 0.0, 0.0);
            when(locationRepository.findById(anyString())).thenAnswer(invocation -> {
                String id = invocation.getArgument(0);
                return "store1".equals(id) ? Optional.of(existing) : Optional.empty();
            });
        FranchiseLocationDataSeeder seeder = new FranchiseLocationDataSeeder(locationRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FranchiseLocation>> locationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(locationRepository).saveAll(locationsCaptor.capture());
        List<FranchiseLocation> locations = locationsCaptor.getValue();
        assertTrue(locations.stream().anyMatch(location -> "store1".equals(location.getId())
            && Double.compare(location.getLatitude(), 40.7128) == 0
            && Double.compare(location.getLongitude(), -74.0060) == 0));
        }

        private FranchiseLocation expectedLocation(int number) {
        return switch (number) {
            case 1 -> new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                "New York", "NY", "10001", "Avery Morgan", 40.7128, -74.0060);
            case 2 -> new FranchiseLocation("store2", "Frosted Corner - Los Angeles", "220 Spring Street",
                "Los Angeles", "CA", "90012", "Jordan Lee", 34.0522, -118.2437);
            case 3 -> new FranchiseLocation("store3", "Frosted Corner - Chicago", "315 Michigan Avenue",
                "Chicago", "IL", "60601", "Taylor Brooks", 41.8781, -87.6298);
            case 4 -> new FranchiseLocation("store4", "Frosted Corner - Houston", "410 Main Street",
                "Houston", "TX", "77002", "Cameron Diaz", 29.7604, -95.3698);
            case 5 -> new FranchiseLocation("store5", "Frosted Corner - Phoenix", "525 Central Avenue",
                "Phoenix", "AZ", "85004", "Riley Carter", 33.4484, -112.0740);
            case 6 -> new FranchiseLocation("store6", "Frosted Corner - Philadelphia", "630 Market Street",
                "Philadelphia", "PA", "19107", "Morgan Reed", 39.9526, -75.1652);
            case 7 -> new FranchiseLocation("store7", "Frosted Corner - San Antonio", "745 Commerce Street",
                "San Antonio", "TX", "78205", "Casey Bennett", 29.4241, -98.4936);
            case 8 -> new FranchiseLocation("store8", "Frosted Corner - San Diego", "850 Broadway",
                "San Diego", "CA", "92101", "Quinn Foster", 32.7157, -117.1611);
            case 9 -> new FranchiseLocation("store9", "Frosted Corner - Dallas", "915 Elm Street",
                "Dallas", "TX", "75201", "Parker Hayes", 32.7767, -96.7970);
            case 10 -> new FranchiseLocation("store10", "Frosted Corner - Jacksonville", "1020 Bay Street",
                "Jacksonville", "FL", "32202", "Reese Collins", 30.3322, -81.6557);
            case 11 -> new FranchiseLocation("store11", "Frosted Corner - Austin", "1125 Congress Avenue",
                "Austin", "TX", "78701", "Skyler Ward", 30.2672, -97.7431);
            case 12 -> new FranchiseLocation("store12", "Frosted Corner - Fort Worth", "1230 Houston Street",
                "Fort Worth", "TX", "76102", "Drew Parker", 32.7555, -97.3308);
            case 13 -> new FranchiseLocation("store13", "Frosted Corner - San Jose", "1335 Santa Clara Street",
                "San Jose", "CA", "95113", "Alexis Turner", 37.3382, -121.8863);
            case 14 -> new FranchiseLocation("store14", "Frosted Corner - Columbus", "1440 High Street",
                "Columbus", "OH", "43215", "Emerson Scott", 39.9612, -82.9988);
            case 15 -> new FranchiseLocation("store15", "Frosted Corner - Charlotte", "1545 Tryon Street",
                "Charlotte", "NC", "28202", "Rowan Kelly", 35.2271, -80.8431);
            case 16 -> new FranchiseLocation("store16", "Frosted Corner - Indianapolis", "1650 Meridian Street",
                "Indianapolis", "IN", "46204", "Finley Price", 39.7684, -86.1581);
            case 17 -> new FranchiseLocation("store17", "Frosted Corner - Seattle", "1755 Pine Street",
                "Seattle", "WA", "98101", "Harper Adams", 47.6062, -122.3321);
            case 18 -> new FranchiseLocation("store18", "Frosted Corner - Denver", "1860 Larimer Street",
                "Denver", "CO", "80202", "Dakota Hughes", 39.7392, -104.9903);
            case 19 -> new FranchiseLocation("store19", "Frosted Corner - Washington", "1965 K Street NW",
                "Washington", "DC", "20001", "Sydney Ross", 38.9072, -77.0369);
            case 20 -> new FranchiseLocation("store20", "Frosted Corner - Nashville", "2070 Broadway",
                "Nashville", "TN", "37203", "Blake Cooper", 36.1627, -86.7816);
            case 21 -> new FranchiseLocation("store21", "Frosted Corner - Oklahoma City", "2175 Sheridan Avenue",
                "Oklahoma City", "OK", "73102", "Kendall Bailey", 35.4676, -97.5164);
            case 22 -> new FranchiseLocation("store22", "Frosted Corner - El Paso", "2280 Mesa Street",
                "El Paso", "TX", "79901", "Jamie Rivera", 31.7619, -106.4850);
            case 23 -> new FranchiseLocation("store23", "Frosted Corner - Boston", "2385 Tremont Street",
                "Boston", "MA", "02108", "Logan Murphy", 42.3601, -71.0589);
            case 24 -> new FranchiseLocation("store24", "Frosted Corner - Portland", "2490 Burnside Street",
                "Portland", "OR", "97205", "Sage Peterson", 45.5152, -122.6784);
            case 25 -> new FranchiseLocation("store25", "Frosted Corner - Las Vegas", "2595 Fremont Street",
                "Las Vegas", "NV", "89101", "Charlie Ramirez", 36.1699, -115.1398);
            case 26 -> new FranchiseLocation("store26", "Frosted Corner - Detroit", "2700 Woodward Avenue",
                "Detroit", "MI", "48226", "Robin Sanders", 42.3314, -83.0458);
            case 27 -> new FranchiseLocation("store27", "Frosted Corner - Memphis", "2805 Union Avenue",
                "Memphis", "TN", "38103", "Ari Simmons", 35.1495, -90.0490);
            case 28 -> new FranchiseLocation("store28", "Frosted Corner - Louisville", "2910 Main Street",
                "Louisville", "KY", "40202", "Hayden Powell", 38.2527, -85.7585);
            case 29 -> new FranchiseLocation("store29", "Frosted Corner - Baltimore", "3015 Charles Street",
                "Baltimore", "MD", "21201", "Marley Long", 39.2904, -76.6122);
            case 30 -> new FranchiseLocation("store30", "Frosted Corner - Milwaukee", "3120 Wisconsin Avenue",
                "Milwaukee", "WI", "53202", "Corey Patterson", 43.0389, -87.9065);
            case 31 -> new FranchiseLocation("store31", "Frosted Corner - Albuquerque", "3225 Central Avenue",
                "Albuquerque", "NM", "87102", "Jules Richardson", 35.0844, -106.6504);
            case 32 -> new FranchiseLocation("store32", "Frosted Corner - Tucson", "3330 Congress Street",
                "Tucson", "AZ", "85701", "Micah Cox", 32.2226, -110.9747);
            case 33 -> new FranchiseLocation("store33", "Frosted Corner - Fresno", "3435 Fulton Street",
                "Fresno", "CA", "93721", "Lane Howard", 36.7378, -119.7871);
            case 34 -> new FranchiseLocation("store34", "Frosted Corner - Sacramento", "3540 Capitol Mall",
                "Sacramento", "CA", "95814", "Shawn Bryant", 38.5816, -121.4944);
            case 35 -> new FranchiseLocation("store35", "Frosted Corner - Kansas City", "3645 Grand Boulevard",
                "Kansas City", "MO", "64106", "Devon Griffin", 39.0997, -94.5786);
            case 36 -> new FranchiseLocation("store36", "Frosted Corner - Atlanta", "3750 Peachtree Street",
                "Atlanta", "GA", "30303", "Ellis Russell", 33.7490, -84.3880);
            case 37 -> new FranchiseLocation("store37", "Frosted Corner - Miami", "3855 Biscayne Boulevard",
                "Miami", "FL", "33130", "Remy Jenkins", 25.7617, -80.1918);
            case 38 -> new FranchiseLocation("store38", "Frosted Corner - Raleigh", "3960 Fayetteville Street",
                "Raleigh", "NC", "27601", "Noel Perry", 35.7796, -78.6382);
            case 39 -> new FranchiseLocation("store39", "Frosted Corner - Omaha", "4065 Farnam Street",
                "Omaha", "NE", "68102", "Frankie Butler", 41.2565, -95.9345);
            case 40 -> new FranchiseLocation("store40", "Frosted Corner - Minneapolis", "4170 Hennepin Avenue",
                "Minneapolis", "MN", "55401", "Kerry Barnes", 44.9778, -93.2650);
            default -> throw new IllegalArgumentException("Unexpected location number: " + number);
        };
    }
}