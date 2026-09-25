package com.frostedcorner.analytics;

import com.frostedcorner.auth.StoreAccessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final StoreAccessService storeAccessService;

    public AnalyticsController(AnalyticsService analyticsService,
                               StoreAccessService storeAccessService) {
        this.analyticsService = analyticsService;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse getSummary(
            @RequestParam(name = "storeId", required = false) String storeId) {
        storeAccessService.requireAnalyticsAccess(storeId);
        return analyticsService.getSummary(storeId);
    }
}