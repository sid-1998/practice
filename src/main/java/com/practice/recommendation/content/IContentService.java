package com.practice.recommendation.content;

import com.practice.recommendation.error.ContentServiceException;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;

import java.util.Collection;
import java.util.List;

/**
 * Hydrates show ids into renderable content.
 *
 * <p>Order-agnostic by contract — hence {@link Collection} rather than {@link List}. Callers that
 * need carousel order must impose it themselves.
 */
public interface IContentService {

    /**
     * Looks up content for the given ids.
     *
     * <p>Ids with no content are logged and omitted, so a partial catalog degrades the carousel
     * rather than failing the request. The result may therefore be smaller than {@code showIds}.
     *
     * @return content for the ids that resolved, in no particular order, never null
     * @throws ContentServiceException if the lookup itself failed
     */
    Collection<Show> fetchContent(RequestContext context, List<String> showIds)
            throws ContentServiceException;
}
