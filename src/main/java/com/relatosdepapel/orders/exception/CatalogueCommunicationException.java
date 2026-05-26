package com.relatosdepapel.orders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CatalogueCommunicationException extends RuntimeException {
    public CatalogueCommunicationException(String message) {
        super(message);
    }
}
