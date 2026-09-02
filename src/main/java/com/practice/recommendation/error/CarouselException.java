package com.practice.recommendation.error;

/**
 * Base type for every failure raised inside this service.
 *
 * <p>Each layer has its own subtype so a caller can tell where a request broke and handle only
 * what its own dependency throws. Every instance carries the {@code requestId} of the request
 * that failed, so a log line and a stack trace can be tied to the same request.
 */
public abstract class CarouselException extends RuntimeException {

    private final String requestId;

    protected CarouselException(String requestId, String message, Throwable cause) {
        super(message + " (requestId=" + requestId + ")", cause);
        this.requestId = requestId;
    }

    public String requestId() {
        return requestId;
    }
}
