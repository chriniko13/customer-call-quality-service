package com.chriniko.customer_call.quality_service.error;

public class ValidationException extends RuntimeException {

    public ValidationException(String s) {
        super(s);
    }
}
