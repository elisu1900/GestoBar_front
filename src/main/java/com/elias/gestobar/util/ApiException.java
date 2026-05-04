package com.elias.gestobar.util;
// utils/ApiException.java
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isUnauthorized() { return statusCode == 401; }
    public boolean isForbidden()    { return statusCode == 403; }
    public boolean isNotFound()     { return statusCode == 404; }
}
