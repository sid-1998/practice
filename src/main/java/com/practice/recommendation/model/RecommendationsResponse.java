package com.practice.recommendation.model;

import java.util.List;

/**
 * The carousel returned to the client, in the order it should be rendered.
 *
 * <p>{@code recommendations} is never null and is always unmodifiable; an empty list means an
 * empty carousel, which is a valid outcome rather than an error.
 */
public record RecommendationsResponse(List<Show> recommendations) {

    private static final RecommendationsResponse EMPTY = new RecommendationsResponse(List.of());

    public RecommendationsResponse {
        if (recommendations == null) {
            throw new IllegalArgumentException("recommendations must not be null");
        }
        recommendations = List.copyOf(recommendations);
    }

    public static RecommendationsResponse empty() {
        return EMPTY;
    }
}
