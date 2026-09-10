package com.nye.backend.common;

public class PhoneNotVerifiedException extends RuntimeException {
    public PhoneNotVerifiedException(String message) {
        super(message);
    }
}
