package com.practice.recommendation.provider;

import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.model.RequestContext;

import java.util.List;

/**
 * The port the core service uses to obtain recommended show ids. The core never sees which model
 * produced them.
 */
public interface IRecommendationProvider {

    /**
     * @return show ids in carousel order; empty when there is nothing to recommend, never null
     * @throws RecommendationProviderException if recommendations could not be produced
     */
    List<String> recommendShowIds(RequestContext context) throws RecommendationProviderException;
}
