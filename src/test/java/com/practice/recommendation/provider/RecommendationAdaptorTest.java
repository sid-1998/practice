package com.practice.recommendation.provider;

import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.provider.model.RecommendationModel;
import com.practice.recommendation.provider.model.StubRecommendationModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationAdaptorTest {

    private static final RequestContext CONTEXT = RequestContext.of("user-1", "en-US");

    private static RecommendationModel modelReturning(List<String> ranking) {
        RecommendationModel model = mock(RecommendationModel.class);
        when(model.modelName()).thenReturn("fake-model");
        when(model.recommend(any())).thenReturn(ranking);
        return model;
    }

    @Test
    void passesTheModelRankingThroughInOrder() {
        RecommendationAdaptor adaptor = new RecommendationAdaptor(
                modelReturning(List.of("s3", "s1", "s2")));

        assertEquals(List.of("s3", "s1", "s2"), adaptor.recommendShowIds(CONTEXT));
    }

    @Test
    void emptyRankingYieldsAnEmptyListNeverNull() {
        RecommendationAdaptor adaptor = new RecommendationAdaptor(modelReturning(List.of()));

        List<String> showIds = adaptor.recommendShowIds(CONTEXT);

        assertNotNull(showIds);
        assertTrue(showIds.isEmpty());
    }

    @Test
    void modelFailureIsTranslatedWithTheOriginalCauseAttached() {
        IllegalStateException modelBlewUp = new IllegalStateException("ranking backend down");
        RecommendationModel model = mock(RecommendationModel.class);
        when(model.modelName()).thenReturn("fake-model");
        when(model.recommend(any())).thenThrow(modelBlewUp);

        RecommendationAdaptor adaptor = new RecommendationAdaptor(model);

        RecommendationProviderException thrown = assertThrows(RecommendationProviderException.class,
                () -> adaptor.recommendShowIds(CONTEXT));

        assertSame(modelBlewUp, thrown.getCause(), "the original failure must not be lost");
        assertEquals(CONTEXT.requestId(), thrown.requestId());
        assertTrue(thrown.getMessage().contains("fake-model"), thrown.getMessage());
    }

    @Test
    void modelReturningNullIsTranslatedRatherThanLeakingANullPointer() {
        RecommendationAdaptor adaptor = new RecommendationAdaptor(modelReturning(null));

        RecommendationProviderException thrown = assertThrows(RecommendationProviderException.class,
                () -> adaptor.recommendShowIds(CONTEXT));

        assertTrue(thrown.getMessage().contains("null"), thrown.getMessage());
    }

    @Test
    void modelReturningANullShowIdIsTranslatedAtTheProviderBoundary() {
        RecommendationAdaptor adaptor = new RecommendationAdaptor(
                modelReturning(Arrays.asList("s1", null)));

        assertThrows(RecommendationProviderException.class,
                () -> adaptor.recommendShowIds(CONTEXT));
    }

    @Test
    void nullContextIsRejected() {
        RecommendationAdaptor adaptor = new RecommendationAdaptor(modelReturning(List.of("s1")));

        assertThrows(IllegalArgumentException.class, () -> adaptor.recommendShowIds(null));
    }

    @Test
    void swappingInAnotherModelChangesTheResultWithNoOtherChange() {
        RequestContext context = RequestContext.of("user-1", "en-US");
        StubRecommendationModel affinity =
                new StubRecommendationModel(Map.of("user-1", List.of("s1", "s2")));
        RecommendationModel trending = new RecommendationModel() {
            @Override
            public String modelName() {
                return "stub-trending-v1";
            }

            @Override
            public List<String> recommend(RequestContext ignored) {
                return List.of("s9");
            }
        };

        IRecommendationProvider viaAffinity = new RecommendationAdaptor(affinity);
        IRecommendationProvider viaTrending = new RecommendationAdaptor(trending);

        assertEquals(List.of("s1", "s2"), viaAffinity.recommendShowIds(context));
        assertEquals(List.of("s9"), viaTrending.recommendShowIds(context));
    }
}
