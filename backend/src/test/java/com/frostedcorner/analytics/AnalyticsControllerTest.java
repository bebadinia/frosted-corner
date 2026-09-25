package com.frostedcorner.analytics;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frostedcorner.auth.SecurityConfig;
import com.frostedcorner.auth.StoreAccessService;
import com.frostedcorner.auth.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

        @MockitoBean
        private StoreAccessService storeAccessService;

        @MockitoBean
        private UserRepository userRepository;

        @Test
        void rejectsUnauthenticatedAnalyticsRequests() throws Exception {
            mockMvc.perform(get("/api/analytics/summary"))
                    .andExpect(status().isUnauthorized());
        }

    @Test
        @WithMockUser(roles = "OWNER")
    void returnsDocumentedSummaryResponseForAllStores() throws Exception {
        AnalyticsSummaryResponse response = new AnalyticsSummaryResponse(2,
                new BigDecimal("20.01"), new BigDecimal("10.01"),
                List.of(new TopSellingProductResponse(
                        "P001", "Chocolate Cake", 3)));
        when(analyticsService.getSummary(null)).thenReturn(response);

        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.revenue").value(20.01))
                .andExpect(jsonPath("$.averageOrderValue").value(10.01))
                .andExpect(jsonPath("$.topSellingProducts[0].productId").value("P001"))
                .andExpect(jsonPath("$.topSellingProducts[0].name").value("Chocolate Cake"))
                .andExpect(jsonPath("$.topSellingProducts[0].quantitySold").value(3));
        verify(analyticsService).getSummary(null);
    }

    @Test
        @WithMockUser(roles = "MANAGER")
    void passesStoreIdToService() throws Exception {
        AnalyticsSummaryResponse response = new AnalyticsSummaryResponse(0,
                new BigDecimal("0.00"), new BigDecimal("0.00"), List.of());
        when(analyticsService.getSummary("store1")).thenReturn(response);

        mockMvc.perform(get("/api/analytics/summary").param("storeId", "store1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.revenue").value(0.00))
                .andExpect(jsonPath("$.averageOrderValue").value(0.00))
                .andExpect(jsonPath("$.topSellingProducts").isEmpty());
        verify(analyticsService).getSummary("store1");
    }

    @Test
        @WithMockUser(roles = "MANAGER")
    void rejectsBlankStoreId() throws Exception {
        when(analyticsService.getSummary(""))
                .thenThrow(new InvalidAnalyticsRequestException("storeId must not be blank"));

        mockMvc.perform(get("/api/analytics/summary").param("storeId", ""))
                .andExpect(status().isBadRequest());
    }
}