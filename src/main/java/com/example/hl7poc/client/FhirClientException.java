package com.example.hl7poc.client;

public class FhirClientException extends RuntimeException {

    private final int statusCode;

    public FhirClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FhirClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
