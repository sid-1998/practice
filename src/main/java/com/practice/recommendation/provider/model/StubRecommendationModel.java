package com.practice.recommendation.provider.model;

import com.practice.recommendation.model.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stub model standing in for a real ranking model: a fixed per-user ranking.
 *
 * <p>A user with no entry gets an empty ranking rather than an error — an empty carousel is a
 * valid outcome. Locale is ignored; this stub ranks on user alone.
 */
public class StubRecommendationModel implements RecommendationModel {

    private static final String MODEL_NAME = "stub-affinity-v1";

    private final Map<String, List<String>> showIdsByUser;

    public StubRecommendationModel(Map<String, List<String>> showIdsByUser) {
        this.showIdsByUser = showIdsByUser.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));
    }

    @Override
    public String modelName() {
        return MODEL_NAME;
    }

    @Override
    public List<String> recommend(RequestContext context) {
        return showIdsByUser.getOrDefault(context.userId(), List.of());
    }
}
