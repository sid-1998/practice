package com.practice.recommendation.model;

import java.util.UUID;

/**
 * Per-request state threaded through every layer, so every log line for one request correlates
 * on {@code requestId}.
 *
 * <p>This is the validation boundary: nothing downstream re-checks {@code userId} or
 * {@code locale}. Construct via {@link #of} rather than the canonical constructor unless the
 * caller already owns a request id.
 */
public record RequestContext(String requestId, String userId, String locale) {

    public RequestContext {
        requireText(requestId, "requestId");
        requireText(userId, "userId");
        requireText(locale, "locale");
    }

    /** Starts a new request, generating the correlation id. */
    public static RequestContext of(String userId, String locale) {
        return new RequestContext(UUID.randomUUID().toString(), userId, locale);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
