package com.practice.recommendation.core;

import com.practice.recommendation.content.IContentService;
import com.practice.recommendation.error.ContentServiceException;
import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.error.RecommendationServiceException;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;
import com.practice.recommendation.provider.IRecommendationProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private static final RequestContext CONTEXT = RequestContext.of("user-1", "en-US");

    private final IRecommendationProvider provider = mock(IRecommendationProvider.class);
    private final IContentService contentService = mock(IContentService.class);
    private final RecommendationService service = new RecommendationService(provider, contentService);

    private static Show show(String id) {
        return new Show(id, "Title " + id, "Description " + id, "http://img/" + id + ".png");
    }

    private static List<String> idsOf(RecommendationsResponse response) {
        return response.recommendations().stream().map(Show::id).toList();
    }

    @Test
    void returnsTheCarouselInRankOrderEvenWhenContentComesBackUnordered() {
        when(provider.recommendShowIds(CONTEXT)).thenReturn(List.of("s3", "s1", "s2"));
        when(contentService.fetchContent(CONTEXT, List.of("s3", "s1", "s2")))
                .thenReturn(Set.of(show("s1"), show("s2"), show("s3")));

        assertEquals(List.of("s3", "s1", "s2"), idsOf(service.getRecommendations(CONTEXT)));
    }

    @Test
    void emptyRankingReturnsAnEmptyCarouselWithoutCallingContent() {
        when(provider.recommendShowIds(CONTEXT)).thenReturn(List.of());

        RecommendationsResponse response = service.getRecommendations(CONTEXT);

        assertTrue(response.recommendations().isEmpty());
        verifyNoInteractions(contentService);
    }

    @Test
    void providerBreakingItsContractWithNullDegradesRatherThanThrowing() {
        when(provider.recommendShowIds(CONTEXT)).thenReturn(null);

        RecommendationsResponse response = service.getRecommendations(CONTEXT);

        assertTrue(response.recommendations().isEmpty());
        verifyNoInteractions(contentService);
    }

    @Test
    void idsWithNoContentAreDroppedAndTheRestKeepTheirOrder() {
        when(provider.recommendShowIds(CONTEXT)).thenReturn(List.of("s1", "s-missing", "s2"));
        when(contentService.fetchContent(any(), any())).thenReturn(List.of(show("s2"), show("s1")));

        assertEquals(List.of("s1", "s2"), idsOf(service.getRecommendations(CONTEXT)));
    }

    @Test
    void providerFailureIsWrappedWithItsCausePreservedAndContentIsNeverCalled() {
        RecommendationProviderException providerFailure =
                new RecommendationProviderException(CONTEXT.requestId(), "model down", new IllegalStateException());
        when(provider.recommendShowIds(CONTEXT)).thenThrow(providerFailure);

        RecommendationServiceException thrown = assertThrows(RecommendationServiceException.class,
                () -> service.getRecommendations(CONTEXT));

        assertSame(providerFailure, thrown.getCause());
        assertEquals(CONTEXT.requestId(), thrown.requestId());
        verify(contentService, never()).fetchContent(any(), any());
    }

    @Test
    void contentFailureIsWrappedAndStaysDistinguishableByCauseType() {
        when(provider.recommendShowIds(CONTEXT)).thenReturn(List.of("s1"));
        when(contentService.fetchContent(any(), any())).thenThrow(
                new ContentServiceException(CONTEXT.requestId(), "catalog down", new IllegalStateException()));

        RecommendationServiceException thrown = assertThrows(RecommendationServiceException.class,
                () -> service.getRecommendations(CONTEXT));

        assertInstanceOf(ContentServiceException.class, thrown.getCause());
    }

    @Test
    void nullContextIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getRecommendations(null));
    }
}
