package com.practice.recommendation.api;

import com.practice.recommendation.error.InvalidTokenException;
import com.practice.recommendation.logging.LogEvents;
import com.practice.recommendation.logging.LogLine;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Stub token resolution: a fixed token-to-user map standing in for real signature verification.
 * Swapping in a real JWT decoder means replacing this class only.
 */
public class StubTokenResolver implements TokenResolver {

    private static final Logger LOG = Logger.getLogger(StubTokenResolver.class.getName());

    private final Map<String, String> userIdsByToken;

    public StubTokenResolver(Map<String, String> userIdsByToken) {
        this.userIdsByToken = Map.copyOf(userIdsByToken);
    }

    @Override
    public String resolveUserId(String requestId, String token) {
        if (token == null || token.isBlank()) {
            LOG.warning(LogLine.of(LogEvents.TOKEN_REJECTED, requestId)
                    .reason(LogEvents.REASON_TOKEN_MISSING).build());
            throw new InvalidTokenException(requestId, "token must not be null or blank");
        }

        String userId = userIdsByToken.get(token);
        if (userId == null) {
            // Deliberately logs no part of the token, not even a prefix.
            LOG.warning(LogLine.of(LogEvents.TOKEN_REJECTED, requestId)
                    .reason(LogEvents.REASON_TOKEN_UNKNOWN).build());
            throw new InvalidTokenException(requestId, "token does not identify a known user");
        }

        LOG.info(LogLine.of(LogEvents.TOKEN_RESOLVED, requestId).userId(userId).build());
        return userId;
    }
}
