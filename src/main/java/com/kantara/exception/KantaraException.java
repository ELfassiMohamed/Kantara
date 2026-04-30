package com.kantara.exception;

public class KantaraException extends RuntimeException {
    public KantaraException(String message) {
        super(message);
    }

    public KantaraException(String message, Throwable cause) {
        super(message, cause);
    }
}
