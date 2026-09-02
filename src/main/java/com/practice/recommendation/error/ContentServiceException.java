package com.practice.recommendation.error;

/**
 * The content layer could not complete a lookup.
 *
 * <p>Not raised for ids that simply have no content — those are logged and omitted so the
 * carousel still renders. This is for the lookup itself failing.
 */
public class ContentServiceException extends CarouselException {

    public ContentServiceException(String requestId, String message, Throwable cause) {
        super(requestId, message, cause);
    }
}
