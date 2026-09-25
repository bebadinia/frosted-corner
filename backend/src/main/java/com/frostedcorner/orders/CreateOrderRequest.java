package com.frostedcorner.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "customerId is required")
        String customerId,
        String storeId,
        CheckoutFulfillmentOption fulfillmentOption,
        @Valid
        CreateOrderCustomerRequest customer,
        @NotEmpty(message = "items are required")
        List<@Valid CreateOrderItemRequest> items) {
}