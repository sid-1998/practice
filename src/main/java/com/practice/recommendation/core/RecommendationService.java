package com.practice.recommendation.core;

import com.practice.recommendation.content.IContentService;
import com.practice.recommendation.error.ContentServiceException;
import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.error.RecommendationServiceException;
import com.practice.recommendation.logging.LogEvents;
import com.practice.recommendation.logging.LogLine;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;
import com.practice.recommendation.provider.IRecommendationProvider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Orchestrates the carousel: ask the provider for a ranking, hydrate it, return it in rank order.
 *
 * <p>Each dependency failure is logged against its own layer and rethrown as
 * {@link RecommendationServiceException} with the cause preserved, so the controller has one type
 * to catch while {@link Throwable#getCause()} still says what broke.
 */
public class RecommendationService implements IRecommendationService {

    private static final Logger LOG = Logger.getLogger(RecommendationService.class.getName());

    private final IRecommendationProvider provider;
    private final IContentService contentService;

    public RecommendationService(IRecommendationProvider provider, IContentService contentService) {
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.contentService = Objects.requireNonNull(contentService, "contentService must not be null");
    }

    @Override
    public RecommendationsResponse getRecommendations(RequestContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        List<String> showIds = rank(context);

        // Nothing to hydrate: an empty ranking means an empty carousel, so the content lookup is
        // skipped entirely rather than called with an empty id list. Null is folded in with empty
        // rather than trusted away, so a provider that breaks its contract degrades to an empty
        // carousel instead of throwing a bare NullPointerException from inside the orchestration.
        if (showIds == null || showIds.isEmpty()) {
            LOG.info(LogLine.of(LogEvents.CAROUSEL_BUILT, context)
                    .with("recommended", 0)
                    .with("hydrated", 0)
                    .reason(LogEvents.REASON_NO_RECOMMENDATIONS)
                    .build());
            return RecommendationsResponse.empty();
        }

        Collection<Show> content = hydrate(context, showIds);
        List<Show> carousel = inRankOrder(showIds, content);

        LOG.info(LogLine.of(LogEvents.CAROUSEL_BUILT, context)
                .with("recommended", showIds.size())
                .with("hydrated", carousel.size())
                .build());
        return new RecommendationsResponse(carousel);
    }

    private List<String> rank(RequestContext context) {
        try {
            return provider.recommendShowIds(context);
        } catch (RecommendationProviderException cause) {
            throw failure(context, LogEvents.LAYER_PROVIDER,
                    "could not obtain recommendations", cause);
        }
    }

    private Collection<Show> hydrate(RequestContext context, List<String> showIds) {
        try {
            return contentService.fetchContent(context, showIds);
        } catch (ContentServiceException cause) {
            throw failure(context, LogEvents.LAYER_CONTENT,
                    "could not hydrate recommendations", cause);
        }
    }

    /**
     * Rehydrates the ranking into shows, keeping the order the model gave.
     *
     * <p>That order is the personalisation and is never recomputed, re-sorted or otherwise altered
     * here — {@code showIds} drives the iteration and the content lookup only supplies the tiles.
     * Ids with no content are dropped, so a partial catalog shortens the carousel instead of
     * failing the request; the surviving shows keep their relative positions.
     */
    private static List<Show> inRankOrder(List<String> showIds, Collection<Show> content) {
        Map<String, Show> byId = content.stream().collect(Collectors.toMap(Show::id,
                Function.identity(), (first, duplicate) -> first, LinkedHashMap::new));
        return showIds.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    private RecommendationServiceException failure(RequestContext context, String layer,
                                                   String message, RuntimeException cause) {
        LOG.log(Level.SEVERE, LogLine.of(LogEvents.CAROUSEL_FAILED, context)
                .layer(layer)
                .build(), cause);
        return new RecommendationServiceException(context.requestId(), message, cause);
    }
}
