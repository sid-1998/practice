package com.practice.recommendation.core;

import com.practice.recommendation.error.RecommendationServiceException;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;

/**
 * The core service behind the controller: obtains recommendations, hydrates them, and returns the
 * carousel in render order.
 */
public interface IRecommendationService {

    /**
     * @return the carousel in render order; possibly empty, never null
     * @throws RecommendationServiceException if a dependency failed; the cause identifies which
     */
    RecommendationsResponse getRecommendations(RequestContext context)
            throws RecommendationServiceException;
}
