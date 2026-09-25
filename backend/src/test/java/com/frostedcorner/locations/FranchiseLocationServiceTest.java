package com.frostedcorner.locations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FranchiseLocationServiceTest {

    @Mock
    private FranchiseLocationRepository locationRepository;

    private FranchiseLocationService franchiseLocationService;

    @BeforeEach
    void setUp() {
        franchiseLocationService = new FranchiseLocationService(
                locationRepository, new DemoCustomerLocationResolver());
    }

    @Test
    void ranksStoresFromClosestToFarthestInMiles() {
        when(locationRepository.findAll()).thenReturn(List.of(
                location("store17", "Seattle", "1755 Pine Street", "WA", "98101", 47.6062, -122.3321),
                location("store24", "Portland", "2490 Burnside Street", "OR", "97205", 45.5152, -122.6784),
                location("store37", "Miami", "3855 Biscayne Boulevard", "FL", "33130", 25.7617, -80.1918)));

        LocationDistanceRankingResponse response = franchiseLocationService.rankStoresByDistance("97205");

        assertEquals("97205", response.customerLocation().matchedDemoLocation());
        assertEquals("store24", response.nearestStore().id());
        assertEquals(0.0, response.nearestStore().distanceMiles());
        assertEquals(List.of("store24", "store17", "store37"),
                response.stores().stream().map(FranchiseLocationDistanceResponse::id).toList());
        assertTrue(response.stores().get(1).distanceMiles() > 100.0);
        assertTrue(response.stores().get(1).distanceMiles() < 200.0);
        assertTrue(response.stores().get(2).distanceMiles() > 2500.0);
    }

    @Test
    void resolvesApprovedDemoAddressStrings() {
        when(locationRepository.findAll()).thenReturn(List.of(
                location("store24", "Portland", "2490 Burnside Street", "OR", "97205", 45.5152, -122.6784),
                location("store17", "Seattle", "1755 Pine Street", "WA", "98101", 47.6062, -122.3321)));

        LocationDistanceRankingResponse response = franchiseLocationService
                .rankStoresByDistance("2490 Burnside Street, Portland, OR 97205");

        assertEquals("2490 Burnside Street, Portland, OR 97205",
                response.customerLocation().matchedDemoLocation());
        assertEquals("store24", response.nearestStore().id());
    }

    @Test
    void listsStoresInStableStoreNameOrder() {
        when(locationRepository.findAll()).thenReturn(List.of(
                location("store17", "Seattle", "1755 Pine Street", "WA", "98101", 47.6062, -122.3321),
                location("store24", "Portland", "2490 Burnside Street", "OR", "97205", 45.5152, -122.6784),
                location("store1", "New York", "101 Broadway", "NY", "10001", 40.7128, -74.0060)));

        List<FranchiseLocation> locations = franchiseLocationService.listStores();

        assertEquals(List.of("store1", "store24", "store17"),
                locations.stream().map(FranchiseLocation::getId).toList());
    }

    @Test
    void rejectsUnsupportedDemoAddressOrZip() {
        assertThrows(UnsupportedDemoLocationException.class,
                () -> franchiseLocationService.rankStoresByDistance("99999"));
    }

    private FranchiseLocation location(String id, String city, String street, String state,
                                       String zipCode, double latitude, double longitude) {
        return new FranchiseLocation(id, "Frosted Corner - " + city, street, city, state,
                zipCode, "Manager", latitude, longitude);
    }
}