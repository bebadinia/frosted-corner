package com.frostedcorner.locations;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLocationLookupException extends RuntimeException {

    public InvalidLocationLookupException(String message) {
        super(message);
    }
}