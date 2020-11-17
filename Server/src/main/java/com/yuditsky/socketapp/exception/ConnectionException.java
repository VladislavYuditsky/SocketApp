package com.yuditsky.socketapp.exception;

public class ConnectionException extends ServiceException {
    public ConnectionException() {
    }

    public ConnectionException(String message, Object... args) {
        super(message, args);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
