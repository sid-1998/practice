package com.practice.recommendation;

import com.practice.recommendation.api.RecommendationController;
import com.practice.recommendation.api.StubTokenResolver;
import com.practice.recommendation.content.StubContentService;
import com.practice.recommendation.core.RecommendationService;
import com.practice.recommendation.error.InvalidTokenException;
import com.practice.recommendation.model.RecommendationsResponse;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.model.Show;
import com.practice.recommendation.provider.RecommendationAdaptor;
import com.practice.recommendation.provider.model.RecommendationModel;
import com.practice.recommendation.provider.model.StubRecommendationModel;

import java.util.List;
import java.util.Map;

/**
 * Wires the stubs together and exercises the request path, so the whole thing can be seen working
 * without an HTTP layer. Everything here is demo data.
 */
public class Main {

    private static final String TOKEN_ALICE = "tok-alice";
    private static final String TOKEN_BOB = "tok-bob";
    private static final String TOKEN_CARA = "tok-cara";

    public static void main(String[] args) {
        Map<String, Show> catalog = Map.of(
                "s1", show("s1", "Mahabharat", "The epic, retold"),
                "s2", show("s2", "Special Ops", "A spy hunts a lead across four cities"),
                "s3", show("s3", "Aarya", "A mother takes over the family business"),
                "s4", show("s4", "Shark Tank", "Founders pitch for investment"));

        // alice's ranking includes s-unlisted, which has no content - the carousel should still
        // render, one tile shorter. bob has no ranking at all, so no content lookup happens.
        StubRecommendationModel model = new StubRecommendationModel(Map.of(
                "user-alice", List.of("s3", "s-unlisted", "s1", "s4"),
                "user-bob", List.of()));

        RecommendationController controller = new RecommendationController(
                new StubTokenResolver(Map.of(
                        TOKEN_ALICE, "user-alice",
                        TOKEN_BOB, "user-bob",
                        TOKEN_CARA, "user-cara")),
                new RecommendationService(
                        new RecommendationAdaptor(model),
                        new StubContentService(catalog)));

        render(controller, "alice - a ranking with one unlisted show", TOKEN_ALICE);
        render(controller, "bob - no recommendations at all", TOKEN_BOB);
        render(controller, "cara - a known token, but the model has never seen her", TOKEN_CARA);
        renderRejection(controller, "an unissued token", "tok-forged");
        render(withBrokenModel(catalog), "the model is down - degrades, does not break", TOKEN_ALICE);
    }

    /** Same wiring, but the model always fails - the carousel should empty rather than throw. */
    private static RecommendationController withBrokenModel(Map<String, Show> catalog) {
        RecommendationModel broken = new RecommendationModel() {
            @Override
            public String modelName() {
                return "always-fails";
            }

            @Override
            public List<String> recommend(RequestContext context) {
                throw new IllegalStateException("ranking backend unreachable");
            }
        };
        return new RecommendationController(
                new StubTokenResolver(Map.of(TOKEN_ALICE, "user-alice")),
                new RecommendationService(
                        new RecommendationAdaptor(broken),
                        new StubContentService(catalog)));
    }

    private static Show show(String id, String title, String description) {
        return new Show(id, title, description, "https://img.example/" + id + ".png");
    }

    private static void render(RecommendationController controller, String scenario, String token) {
        System.out.println();
        System.out.println("=== " + scenario + " ===");
        RecommendationsResponse response = controller.getRecommendations(token, "en-IN");
        if (response.recommendations().isEmpty()) {
            System.out.println("  (empty carousel)");
            return;
        }
        response.recommendations()
                .forEach(show -> System.out.println("  " + show.id() + "  " + show.title()
                        + " - " + show.description()));
    }

    private static void renderRejection(RecommendationController controller, String scenario,
                                        String token) {
        System.out.println();
        System.out.println("=== " + scenario + " ===");
        try {
            controller.getRecommendations(token, "en-IN");
            System.out.println("  unexpectedly accepted");
        } catch (InvalidTokenException rejected) {
            System.out.println("  rejected loudly: " + rejected.getMessage());
        }
    }
}
