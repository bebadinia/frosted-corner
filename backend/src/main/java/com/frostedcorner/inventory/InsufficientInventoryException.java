package com.frostedcorner.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String storeId, String productId,
                                          int requested, int available) {
        super("Insufficient inventory for store " + storeId + " and product " + productId
                + ": requested " + requested + ", available " + available);
    }
}