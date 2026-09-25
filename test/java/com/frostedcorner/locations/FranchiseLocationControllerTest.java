package com.frostedcorner.locations;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FranchiseLocationController.class)
class FranchiseLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FranchiseLocationService franchiseLocationService;

    @Test
    void returnsNearestStoreAndRankedLocations() throws Exception {
        LocationDistanceRankingResponse response = new LocationDistanceRankingResponse(
                new DemoCustomerCoordinates("97205", 45.5152, -122.6784),
                new FranchiseLocationDistanceResponse("store24", "Frosted Corner - Portland",
                        "2490 Burnside Street", "Portland", "OR", "97205", "Sage Peterson",
                        45.5152, -122.6784, 0.0),
                List.of(
                        new FranchiseLocationDistanceResponse("store24", "Frosted Corner - Portland",
                                "2490 Burnside Street", "Portland", "OR", "97205", "Sage Peterson",
                                45.5152, -122.6784, 0.0),
                        new FranchiseLocationDistanceResponse("store17", "Frosted Corner - Seattle",
                                "1755 Pine Street", "Seattle", "WA", "98101", "Harper Adams",
                                47.6062, -122.3321, 145.42)));
        when(franchiseLocationService.rankStoresByDistance("97205")).thenReturn(response);

        mockMvc.perform(get("/api/locations/nearest").param("demoAddressOrZip", "97205"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerLocation.matchedDemoLocation").value("97205"))
                .andExpect(jsonPath("$.nearestStore.id").value("store24"))
                .andExpect(jsonPath("$.nearestStore.distanceMiles").value(0.0))
                .andExpect(jsonPath("$.stores[0].id").value("store24"))
                .andExpect(jsonPath("$.stores[1].id").value("store17"));
    }

    @Test
    void returnsSelectableStoreList() throws Exception {
        when(franchiseLocationService.listStores()).thenReturn(List.of(
                new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                        "New York", "NY", "10001", "Morgan Lee", 40.7128, -74.0060),
                new FranchiseLocation("store24", "Frosted Corner - Portland", "2490 Burnside Street",
                        "Portland", "OR", "97205", "Sage Peterson", 45.5152, -122.6784)));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("store1"))
                .andExpect(jsonPath("$[1].id").value("store24"));
    }

    @Test
    void rejectsUnsupportedDemoLocation() throws Exception {
        when(franchiseLocationService.rankStoresByDistance("99999"))
                .thenThrow(new UnsupportedDemoLocationException("99999"));

        mockMvc.perform(get("/api/locations/nearest").param("demoAddressOrZip", "99999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresDemoAddressOrZip() throws Exception {
        mockMvc.perform(get("/api/locations/nearest"))
                .andExpect(status().isBadRequest());
    }
}