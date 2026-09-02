package com.practice.recommendation.api;

import com.practice.recommendation.error.InvalidTokenException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubTokenResolverTest {

    private static final String SECRET_TOKEN = "tok-abc123";
    private static final String REQUEST_ID = "req-1";

    private final StubTokenResolver resolver =
            new StubTokenResolver(Map.of(SECRET_TOKEN, "user-1"));

    @Test
    void knownTokenResolvesToItsUser() {
        assertEquals("user-1", resolver.resolveUserId(REQUEST_ID, SECRET_TOKEN));
    }

    @Test
    void blankTokenIsRejectedBeforeAnyLookup() {
        InvalidTokenException thrown = assertThrows(InvalidTokenException.class,
                () -> resolver.resolveUserId(REQUEST_ID, "   "));

        // The distinct message proves the guard short-circuited rather than falling through to a
        // map miss, which would have reported "does not identify a known user".
        assertTrue(thrown.getMessage().contains("must not be null or blank"), thrown.getMessage());
    }

    @Test
    void unknownTokenIsRejectedWithRequestIdButNeverTheTokenValue() {
        InvalidTokenException thrown = assertThrows(InvalidTokenException.class,
                () -> resolver.resolveUserId(REQUEST_ID, "tok-not-issued"));

        assertEquals(REQUEST_ID, thrown.requestId());
        assertTrue(thrown.getMessage().contains(REQUEST_ID), thrown.getMessage());
        assertFalse(thrown.getMessage().contains("tok-not-issued"),
                "the token is a credential and must never reach a message or log: "
                        + thrown.getMessage());
    }
}
