package com.frostedcorner.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String storeId, String productId) {
        super("Inventory not found for store " + storeId + " and product " + productId);
    }
}