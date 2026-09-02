package com.practice.recommendation.provider;

import com.practice.recommendation.error.RecommendationProviderException;
import com.practice.recommendation.logging.LogEvents;
import com.practice.recommendation.logging.LogLine;
import com.practice.recommendation.model.RequestContext;
import com.practice.recommendation.provider.model.RecommendationModel;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The only {@link IRecommendationProvider}: adapts whichever {@link RecommendationModel} is wired
 * in to the port the core service consumes.
 *
 * <p>Everything model-independent lives here — input guarding, logging which model served the
 * request, and translating any model failure into {@link RecommendationProviderException}. That
 * keeps a new model down to one class plus one wiring change, with no edit to the core service.
 *
 * <p>The model call is in-process, so there is nothing to time out. Once a model becomes a network
 * hop, its timeout and retry decision belong on the {@code model.recommend} call below.
 */
public class RecommendationAdaptor implements IRecommendationProvider {

    private static final Logger LOG = Logger.getLogger(RecommendationAdaptor.class.getName());

    private final RecommendationModel model;

    public RecommendationAdaptor(RecommendationModel model) {
        this.model = Objects.requireNonNull(model, "model must not be null");
    }

    @Override
    public List<String> recommendShowIds(RequestContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        try {
            List<String> ranked = model.recommend(context);
            if (ranked == null) {
                throw failure(context, LogEvents.REASON_NULL_RANKING,
                        "returned null instead of a ranking", null);
            }
            // Rejects null elements, so a misbehaving model fails here rather than in the
            // content layer where the origin would be much harder to see.
            List<String> showIds = List.copyOf(ranked);

            // An empty ranking is a normal outcome, but logged distinctly so an empty carousel
            // is traceable to the model having nothing rather than to a lookup that went wrong.
            LogLine ranking = LogLine.of(LogEvents.RECOMMENDATIONS_RANKED, context)
                    .with("model", model.modelName())
                    .with("count", showIds.size());
            if (showIds.isEmpty()) {
                ranking.reason(LogEvents.REASON_NO_RECOMMENDATIONS);
            }
            LOG.info(ranking.build());
            return showIds;
        } catch (RecommendationProviderException alreadyTranslated) {
            throw alreadyTranslated;
        } catch (RuntimeException cause) {
            throw failure(context, LogEvents.REASON_MODEL_ERROR, "failed while ranking", cause);
        }
    }

    private RecommendationProviderException failure(RequestContext context, String reason,
                                                    String what, RuntimeException cause) {
        LOG.log(Level.SEVERE, LogLine.of(LogEvents.RECOMMENDATIONS_FAILED, context)
                .with("model", model.modelName())
                .reason(reason)
                .build(), cause);
        return new RecommendationProviderException(context.requestId(),
                "model " + model.modelName() + " " + what, cause);
    }
}
