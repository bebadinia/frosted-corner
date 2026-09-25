package com.frostedcorner.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInventoryQuantityException extends RuntimeException {

    public InvalidInventoryQuantityException() {
        super("quantity must be zero or greater");
    }
}