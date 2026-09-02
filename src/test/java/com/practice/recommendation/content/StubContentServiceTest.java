package com.practice.recommendation.content;

import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubContentServiceTest {

    private static final RequestContext CONTEXT = RequestContext.of("user-1", "en-US");

    private static Show show(String id) {
        return new Show(id, "Title " + id, "Description " + id, "http://img/" + id + ".png");
    }

    private final StubContentService contentService = new StubContentService(
            Map.of("s1", show("s1"), "s2", show("s2"), "s3", show("s3")));

    private final List<LogRecord> logged = new ArrayList<>();
    private final Logger serviceLogger = Logger.getLogger(StubContentService.class.getName());
    private Handler capture;

    @BeforeEach
    void captureLogs() {
        capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logged.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        serviceLogger.addHandler(capture);
    }

    @AfterEach
    void releaseLogs() {
        serviceLogger.removeHandler(capture);
    }

    private List<String> warningsAbout(String showId) {
        return logged.stream()
                .filter(record -> record.getLevel() == Level.WARNING)
                .map(LogRecord::getMessage)
                .filter(message -> message.contains("showId=" + showId))
                .toList();
    }

    private static List<String> idsOf(Collection<Show> content) {
        return content.stream().map(Show::id).sorted().toList();
    }

    @Test
    void hydratesEveryRequestedId() {
        Collection<Show> content = contentService.fetchContent(CONTEXT, List.of("s2", "s1"));

        assertEquals(List.of("s1", "s2"), idsOf(content));
        assertEquals(show("s1"), content.stream().filter(s -> s.id().equals("s1")).findFirst().orElseThrow());
        assertTrue(warningsAbout("s1").isEmpty(), "nothing was missing, so nothing should warn");
    }

    @Test
    void emptyRequestReturnsEmptyWithoutWarningAboutAnything() {
        Collection<Show> content = contentService.fetchContent(CONTEXT, List.of());

        assertTrue(content.isEmpty());
        assertTrue(logged.stream().noneMatch(record -> record.getLevel() == Level.WARNING),
                "an empty request has no gaps to report");
    }

    @Test
    void everyIdWithNoContentIsWarnedAboutIndividuallyAndOmittedFromTheResult() {
        Collection<Show> content = contentService.fetchContent(CONTEXT,
                List.of("s1", "s-gone", "s2", "s-also-gone"));

        assertEquals(List.of("s1", "s2"), idsOf(content));

        assertEquals(1, warningsAbout("s-gone").size(), "one warning naming s-gone");
        assertEquals(1, warningsAbout("s-also-gone").size(), "one warning naming s-also-gone");
        assertTrue(warningsAbout("s-gone").get(0).contains("reason=content_not_found"),
                warningsAbout("s-gone").get(0));
        assertTrue(warningsAbout("s-gone").get(0).contains(CONTEXT.requestId()),
                "the warning must correlate to the request");
    }

    @Test
    void theSummaryLineCountsRequestedFoundAndMissing() {
        contentService.fetchContent(CONTEXT, List.of("s1", "s-gone", "s2"));

        String summary = logged.stream()
                .map(LogRecord::getMessage)
                .filter(message -> message.contains("event=content_fetched"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no summary line was logged"));

        assertTrue(summary.contains("requested=3"), summary);
        assertTrue(summary.contains("found=2"), summary);
        assertTrue(summary.contains("missing=1"), summary);
    }

    @Test
    void nullShowIdsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> contentService.fetchContent(CONTEXT, null));
    }

    @Test
    void aCatalogKeyedInconsistentlyWithItsShowIsRejectedAtConstruction() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new StubContentService(Map.of("s1", show("s2"))));

        assertTrue(thrown.getMessage().contains("does not match"), thrown.getMessage());
    }
}
