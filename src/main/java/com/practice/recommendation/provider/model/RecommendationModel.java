package com.practice.recommendation.provider.model;

import com.practice.recommendation.model.RequestContext;

import java.util.List;

/**
 * A single recommendation model — the swappable unit behind the adaptor.
 *
 * <p>Implementations only rank; they own no logging, guarding or error translation. That belongs
 * to the adaptor, so a new model is one class and one wiring change and nothing else.
 */
public interface RecommendationModel {

    /** Stable identifier for this model, logged on every request it serves. */
    String modelName();

    /**
     * Ranks shows for the given user, best first.
     *
     * @return show ids in carousel order; empty when this model has nothing for the user, never null
     * @throws RuntimeException if the model itself fails; the adaptor translates it
     */
    List<String> recommend(RequestContext context);
}
