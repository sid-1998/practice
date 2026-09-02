package com.practice.recommendation.api;

import com.practice.recommendation.core.IRecommendationService;
import com.practice.recommendation.error.InvalidTokenException;
import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.error.RecommendationServiceException;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RecommendationControllerTest {

    private static final String TOKEN = "tok-abc123";
    private static final Show SHOW = new Show("s1", "Show One", "desc", "http://img/s1.png");

    private final IRecommendationService recommendationService = mock(IRecommendationService.class);
    private final RecommendationController controller = new RecommendationController(
            new StubTokenResolver(Map.of(TOKEN, "user-1")), recommendationService);

    private static RecommendationServiceException dependencyFailure() {
        return new RecommendationServiceException("req-1", "provider down",
                new RecommendationProviderException("req-1", "model down", new IllegalStateException()));
    }

    @Test
    void decodesTheTokenIntoTheContextAndReturnsTheCarousel() {
        when(recommendationService.getRecommendations(any()))
                .thenReturn(new RecommendationsResponse(List.of(SHOW)));

        RecommendationsResponse response = controller.getRecommendations(TOKEN, "en-US");

        assertEquals(List.of(SHOW), response.recommendations());

        ArgumentCaptor<RequestContext> context = ArgumentCaptor.forClass(RequestContext.class);
        verify(recommendationService).getRecommendations(context.capture());
        assertEquals("user-1", context.getValue().userId(), "the decoded user id must reach the context");
        assertEquals("en-US", context.getValue().locale());
        assertFalse(context.getValue().requestId().isBlank());
    }

    @Test
    void aDependencyFailureDegradesToAnEmptyCarouselInsteadOfBreakingThePage() {
        when(recommendationService.getRecommendations(any())).thenThrow(dependencyFailure());

        RecommendationsResponse response = controller.getRecommendations(TOKEN, "en-US");

        assertTrue(response.recommendations().isEmpty());
    }

    @Test
    void anUnknownTokenThrowsAndNeverReachesTheRecommendationService() {
        assertThrows(InvalidTokenException.class,
                () -> controller.getRecommendations("tok-not-issued", "en-US"));

        verifyNoInteractions(recommendationService);
    }

    @Test
    void aMissingLocaleThrowsBeforeAnyDownstreamWork() {
        assertThrows(IllegalArgumentException.class, () -> controller.getRecommendations(TOKEN, "  "));

        verify(recommendationService, never()).getRecommendations(any());
    }
}
