package com.practice.recommendation.content;

import com.practice.recommendation.logging.LogEvents;
import com.practice.recommendation.logging.LogLine;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stub content lookup: a fixed in-memory catalog standing in for the content service.
 *
 * <p>An id with no content is logged individually and left out of the result, so one gap in the
 * catalog costs a single tile rather than the whole carousel. The count of gaps is worth watching:
 * a rising {@code missing} means the recommendation model and the catalog have drifted apart.
 *
 * <p>Being a map lookup, this stub has no way for the lookup itself to fail, so it never throws
 * {@link com.practice.recommendation.error.ContentServiceException} — a real implementation over
 * HTTP would, and that is where a timeout and retry decision would attach.
 */
public class StubContentService implements IContentService {

    private static final Logger LOG = Logger.getLogger(StubContentService.class.getName());

    private final Map<String, Show> catalog;

    /**
     * @param catalog content keyed by show id
     * @throws IllegalArgumentException if a key does not match the id of the show it maps to,
     *         which would make lookups silently return the wrong tile
     */
    public StubContentService(Map<String, Show> catalog) {
        catalog.forEach((id, show) -> {
            if (!id.equals(show.id())) {
                throw new IllegalArgumentException(
                        "catalog key " + id + " does not match show id " + show.id());
            }
        });
        this.catalog = Map.copyOf(catalog);
    }

    @Override
    public Collection<Show> fetchContent(RequestContext context, List<String> showIds) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (showIds == null) {
            throw new IllegalArgumentException("showIds must not be null");
        }

        List<Show> found = new ArrayList<>(showIds.size());
        for (String showId : showIds) {
            Show show = catalog.get(showId);
            if (show == null) {
                // One line per gap, naming the id, so a specific missing show is greppable rather
                // than hidden inside an aggregate count.
                LOG.warning(LogLine.of(LogEvents.CONTENT_MISSING, context)
                        .with("showId", showId)
                        .reason(LogEvents.REASON_CONTENT_NOT_FOUND)
                        .build());
                continue;
            }
            found.add(show);
        }

        LOG.info(LogLine.of(LogEvents.CONTENT_FETCHED, context)
                .with("requested", showIds.size())
                .with("found", found.size())
                .with("missing", showIds.size() - found.size())
                .build());
        return List.copyOf(found);
    }
}
