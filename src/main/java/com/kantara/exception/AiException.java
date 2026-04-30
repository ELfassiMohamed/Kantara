package com.kantara.exception;

public class AiException extends KantaraException {
    private final int httpStatus;

    public AiException(String message) {
        super(message);
        this.httpStatus = -1;
    }

    public AiException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = -1;
    }

    public boolean isRetryable() {
        return httpStatus == -1           // network error
            || httpStatus == 429          // rate limited
            || httpStatus >= 500;         // server error
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
