package com.practice.recommendation.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationsResponseTest {

    private static final Show SHOW = new Show("s1", "Show One", "desc", "http://img/s1.png");

    @Test
    void keepsShowsInTheGivenOrder() {
        Show second = new Show("s2", "Show Two", "desc", "http://img/s2.png");

        RecommendationsResponse response = new RecommendationsResponse(List.of(SHOW, second));

        assertEquals(List.of("s1", "s2"), response.recommendations().stream().map(Show::id).toList());
    }

    @Test
    void emptyCarouselIsValidAndNotAnError() {
        assertTrue(RecommendationsResponse.empty().recommendations().isEmpty());
    }

    @Test
    void copiesTheSourceListSoLaterMutationCannotChangeTheResponse() {
        List<Show> mutable = new ArrayList<>(List.of(SHOW));
        RecommendationsResponse response = new RecommendationsResponse(mutable);

        mutable.clear();

        assertEquals(1, response.recommendations().size());
        assertThrows(UnsupportedOperationException.class, () -> response.recommendations().clear());
    }

    @Test
    void nullRecommendationsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RecommendationsResponse(null));
    }
}
