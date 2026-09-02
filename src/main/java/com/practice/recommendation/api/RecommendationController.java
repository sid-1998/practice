package com.practice.recommendation.api;

import com.practice.recommendation.core.IRecommendationService;
import com.practice.recommendation.error.RecommendationServiceException;
import com.practice.recommendation.logging.LogEvents;
import com.practice.recommendation.logging.LogLine;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for {@code GET /recommendations?locale=<locale>}.
 *
 * <p>The request boundary: it validates the caller's input, decodes the token into the user id and
 * stores it in a {@link RequestContext} — the only place a user id enters the system.
 *
 * <p>It is also the one place a failure is turned into a degraded result rather than propagated.
 * A dependency failure returns an empty carousel after an ERROR log, because an empty carousel
 * costs one shelf while a thrown error breaks the whole page. Client errors are the exception to
 * the exception: a bad token or a missing locale still throws, since answering them with an empty
 * carousel would hide the caller's bug.
 */
public class RecommendationController {

    private static final Logger LOG = Logger.getLogger(RecommendationController.class.getName());

    private final TokenResolver tokenResolver;
    private final IRecommendationService recommendationService;

    public RecommendationController(TokenResolver tokenResolver,
                                    IRecommendationService recommendationService) {
        this.tokenResolver = Objects.requireNonNull(tokenResolver, "tokenResolver must not be null");
        this.recommendationService =
                Objects.requireNonNull(recommendationService, "recommendationService must not be null");
    }

    /**
     * @param token the caller's opaque token, which identifies the user
     * @param locale the locale the carousel should be rendered for
     * @return the carousel, or an empty one if a dependency failed; never null
     * @throws com.practice.recommendation.error.InvalidTokenException if the token is absent or unknown
     * @throws IllegalArgumentException if the locale is absent
     */
    public RecommendationsResponse getRecommendations(String token, String locale) {
        String requestId = UUID.randomUUID().toString();

        // Checked before the token is decoded, so a malformed request costs no downstream work.
        if (locale == null || locale.isBlank()) {
            throw new IllegalArgumentException("locale must not be null or blank");
        }

        String userId = tokenResolver.resolveUserId(requestId, token);
        RequestContext context = new RequestContext(requestId, userId, locale);

        try {
            return recommendationService.getRecommendations(context);
        } catch (RecommendationServiceException dependencyFailed) {
            LOG.log(Level.SEVERE, LogLine.of(LogEvents.CAROUSEL_DEGRADED, context)
                    .reason(LogEvents.REASON_DEPENDENCY_FAILURE)
                    .build(), dependencyFailed);
            return RecommendationsResponse.empty();
        }
    }
}
