package com.practice.recommendation.api;

import com.practice.recommendation.error.InvalidTokenException;

/**
 * Decodes a caller's opaque token into the user it identifies.
 *
 * <p>Takes the {@code requestId} explicitly rather than a
 * {@link com.practice.recommendation.model.RequestContext}: the context cannot be built until the
 * user id is known, so the controller generates the correlation id first and passes it in here.
 */
public interface TokenResolver {

    /**
     * @return the user id the token identifies, never null or blank
     * @throws InvalidTokenException if the token is absent, malformed or unknown. Implementations
     *         must not put the token value in the message — it is a credential.
     */
    String resolveUserId(String requestId, String token) throws InvalidTokenException;
}
