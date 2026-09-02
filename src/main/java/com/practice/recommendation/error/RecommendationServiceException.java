package com.practice.recommendation.error;

/**
 * Orchestration failed. Wraps {@link RecommendationProviderException} or
 * {@link ContentServiceException} so the controller has one type to catch, while
 * {@link #getCause()} still says which dependency broke.
 */
public class RecommendationServiceException extends CarouselException {

    public RecommendationServiceException(String requestId, String message, Throwable cause) {
        super(requestId, message, cause);
    }
}
