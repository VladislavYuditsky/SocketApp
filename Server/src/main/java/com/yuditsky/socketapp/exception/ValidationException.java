package com.yuditsky.socketapp.exception;

public class ValidationException extends ServiceException {
    public ValidationException() {
    }

    public ValidationException(String message, Object... args) {
        super(message, args);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
