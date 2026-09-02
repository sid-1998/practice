package com.practice.recommendation.provider.model;

import com.practice.recommendation.model.RequestContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubRecommendationModelTest {

    @Test
    void ranksTheConfiguredShowsForAKnownUser() {
        StubRecommendationModel model =
                new StubRecommendationModel(Map.of("user-1", List.of("s3", "s1", "s2")));

        assertEquals(List.of("s3", "s1", "s2"),
                model.recommend(RequestContext.of("user-1", "en-US")));
    }

    @Test
    void unknownUserGetsAnEmptyRankingRatherThanAnError() {
        StubRecommendationModel model =
                new StubRecommendationModel(Map.of("user-1", List.of("s1")));

        assertTrue(model.recommend(RequestContext.of("user-nobody", "en-US")).isEmpty());
    }

    @Test
    void copiesTheConfiguredRankingSoLaterMutationCannotChangeIt() {
        List<String> mutable = new ArrayList<>(List.of("s1", "s2"));
        StubRecommendationModel model = new StubRecommendationModel(Map.of("user-1", mutable));

        mutable.clear();

        assertEquals(List.of("s1", "s2"), model.recommend(RequestContext.of("user-1", "en-US")));
    }
}
