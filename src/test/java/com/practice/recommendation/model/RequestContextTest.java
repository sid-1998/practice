package com.practice.recommendation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestContextTest {

    @Test
    void ofBuildsContextAndGeneratesCorrelationId() {
        RequestContext context = RequestContext.of("user-1", "en-US");

        assertEquals("user-1", context.userId());
        assertEquals("en-US", context.locale());
        assertFalse(context.requestId().isBlank(), "requestId should be generated");
        assertNotEquals(context.requestId(), RequestContext.of("user-1", "en-US").requestId(),
                "each request should get its own correlation id");
    }

    @Test
    void blankLocaleIsRejected() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> RequestContext.of("user-1", "   "));

        assertTrue(thrown.getMessage().contains("locale"), thrown.getMessage());
    }

    @Test
    void nullUserIdIsRejectedNamingTheField() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> RequestContext.of(null, "en-US"));

        assertTrue(thrown.getMessage().contains("userId"), thrown.getMessage());
    }
}
