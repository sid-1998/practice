package com.practice.recommendation.error;

/**
 * The recommendation adaptor could not produce show ids — the underlying model failed or
 * misbehaved. Always carries the original failure as its cause.
 */
public class RecommendationProviderException extends CarouselException {

    public RecommendationProviderException(String requestId, String message, Throwable cause) {
        super(requestId, message, cause);
    }
}
