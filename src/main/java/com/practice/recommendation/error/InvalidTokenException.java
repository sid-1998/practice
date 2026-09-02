package com.practice.recommendation.error;

/**
 * The caller's token could not be decoded into a user.
 *
 * <p>A client error, not a dependency failure: it propagates out of the controller rather than
 * degrading to an empty carousel, because a silently empty response would hide the bug from the
 * caller. The message never contains the token itself.
 */
public class InvalidTokenException extends CarouselException {

    public InvalidTokenException(String requestId, String message) {
        super(requestId, message, null);
    }
}
