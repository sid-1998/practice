package com.practice.recommendation.logging;

/**
 * The vocabulary of the structured logs: every {@code event}, {@code reason} and {@code layer}
 * value this service emits.
 *
 * <p>Centralised so a log-based alert or dashboard has one place to read the valid values from,
 * and so renaming an event is a compile-time change rather than a grep.
 */
public final class LogEvents {

    public static final String TOKEN_RESOLVED = "token_resolved";
    public static final String TOKEN_REJECTED = "token_rejected";
    public static final String RECOMMENDATIONS_RANKED = "recommendations_ranked";
    public static final String RECOMMENDATIONS_FAILED = "recommendations_failed";
    public static final String CONTENT_FETCHED = "content_fetched";
    public static final String CONTENT_MISSING = "content_missing";
    public static final String CAROUSEL_BUILT = "carousel_built";
    public static final String CAROUSEL_FAILED = "carousel_failed";
    public static final String CAROUSEL_DEGRADED = "carousel_degraded";

    public static final String REASON_TOKEN_MISSING = "token_missing";
    public static final String REASON_TOKEN_UNKNOWN = "token_unknown";
    public static final String REASON_NULL_RANKING = "null_ranking";
    public static final String REASON_MODEL_ERROR = "model_error";
    public static final String REASON_NO_RECOMMENDATIONS = "no_recommendations";
    public static final String REASON_CONTENT_NOT_FOUND = "content_not_found";
    public static final String REASON_DEPENDENCY_FAILURE = "dependency_failure";

    public static final String LAYER_PROVIDER = "provider";
    public static final String LAYER_CONTENT = "content";

    private LogEvents() {
    }
}
