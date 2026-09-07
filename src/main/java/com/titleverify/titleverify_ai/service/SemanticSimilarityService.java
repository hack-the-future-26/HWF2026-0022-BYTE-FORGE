package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalDouble;

@Service
public class SemanticSimilarityService {

    // Development thresholds (experimental development values, not official PRGI thresholds)
    public static final double HIGH_SEMANTIC_THRESHOLD = 0.85;
    public static final double MODERATE_SEMANTIC_THRESHOLD = 0.65;

    private final EmbeddingService embeddingService;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public SemanticSimilarityService(EmbeddingService embeddingService,
                                     CosineSimilarityCalculator cosineSimilarityCalculator) {
        this.embeddingService = embeddingService;
        this.cosineSimilarityCalculator = cosineSimilarityCalculator;
    }

    /**
     * Calculates semantic similarity between two titles using vector embeddings and Cosine Similarity.
     * Returns OptionalDouble containing similarity score between 0.0 and 1.0, or OptionalDouble.empty() if unavailable.
     */
    public OptionalDouble calculateSemanticSimilarity(String titleA, String titleB) {
        if (titleA == null || titleB == null) {
            return OptionalDouble.empty();
        }
        String trimmedA = titleA.trim();
        String trimmedB = titleB.trim();

        if (trimmedA.isEmpty() || trimmedB.isEmpty()) {
            if (trimmedA.isEmpty() && trimmedB.isEmpty()) {
                return OptionalDouble.of(1.0);
            }
            return OptionalDouble.of(0.0);
        }
        if (trimmedA.equalsIgnoreCase(trimmedB)) {
            return OptionalDouble.of(1.0);
        }

        Optional<float[]> vecA = embeddingService.getEmbedding(titleA);
        Optional<float[]> vecB = embeddingService.getEmbedding(titleB);

        if (vecA.isEmpty() || vecB.isEmpty()) {
            return OptionalDouble.empty();
        }

        double similarity = cosineSimilarityCalculator.calculateCosineSimilarity(vecA.get(), vecB.get());
        return OptionalDouble.of(similarity);
    }

    /**
     * Categorizes semantic similarity score into experimental development threshold levels.
     */
    public String classifySemanticLevel(double score) {
        if (score >= HIGH_SEMANTIC_THRESHOLD) {
            return "HIGH";
        } else if (score >= MODERATE_SEMANTIC_THRESHOLD) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    public boolean isAvailable() {
        return embeddingService.isAvailable();
    }
}
