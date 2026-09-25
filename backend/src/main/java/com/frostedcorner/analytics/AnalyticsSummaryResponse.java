package com.frostedcorner.analytics;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsSummaryResponse(
        long totalOrders,
        BigDecimal revenue,
        BigDecimal averageOrderValue,
        List<TopSellingProductResponse> topSellingProducts) {
}