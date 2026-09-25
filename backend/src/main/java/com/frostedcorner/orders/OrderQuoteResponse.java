package com.frostedcorner.orders;

import java.math.BigDecimal;

public record OrderQuoteResponse(
        String fulfillmentType,
        String storeId,
        BigDecimal subtotal,
        BigDecimal standardFulfillmentFee,
        BigDecimal fulfillmentFee,
        BigDecimal promotionSavings,
        boolean promotionApplied,
        BigDecimal estimatedTotal) {
}
