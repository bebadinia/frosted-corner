package com.frostedcorner.orders;

import java.math.BigDecimal;

public record OrderQuoteResponse(
        BigDecimal subtotal,
        BigDecimal fulfillmentFee,
        BigDecimal total,
        String fulfillmentType,
        boolean promotionApplied) {
}
