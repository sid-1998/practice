package com.practice.recommendation.logging;

import com.practice.recommendation.model.RequestContext;

/**
 * Builds one structured log line as space-separated {@code key=value} pairs, always starting with
 * {@code event} and the correlation id.
 *
 * <p>Owns the format so all eight log sites stay parseable in the same way. Values are expected to
 * be single tokens — ids, counts, and the constants in {@link LogEvents} — since a value
 * containing a space would break field parsing. Never pass a credential or a request body.
 */
public final class LogLine {

    private static final String EVENT = "event";
    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String REASON = "reason";
    private static final String LAYER = "layer";

    private final StringBuilder line;

    private LogLine(String event) {
        this.line = new StringBuilder(EVENT).append('=').append(event);
    }

    /** For anything happening after the user is known. */
    public static LogLine of(String event, RequestContext context) {
        return new LogLine(event)
                .with(REQUEST_ID, context.requestId())
                .with(USER_ID, context.userId());
    }

    /** For token resolution, which runs before a {@link RequestContext} can exist. */
    public static LogLine of(String event, String requestId) {
        return new LogLine(event).with(REQUEST_ID, requestId);
    }

    /** For token resolution, which learns the user id without holding a context. */
    public LogLine userId(String userId) {
        return with(USER_ID, userId);
    }

    public LogLine with(String key, Object value) {
        line.append(' ').append(key).append('=').append(value);
        return this;
    }

    /** Why an event happened — use a {@code REASON_*} constant from {@link LogEvents}. */
    public LogLine reason(String reason) {
        return with(REASON, reason);
    }

    /** Which layer failed — use a {@code LAYER_*} constant from {@link LogEvents}. */
    public LogLine layer(String layer) {
        return with(LAYER, layer);
    }

    public String build() {
        return line.toString();
    }
}
