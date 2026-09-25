package com.frostedcorner.locations;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedDemoLocationException extends RuntimeException {

    public UnsupportedDemoLocationException(String demoAddressOrZip) {
        super("Unsupported demo address or ZIP: " + demoAddressOrZip);
    }
}