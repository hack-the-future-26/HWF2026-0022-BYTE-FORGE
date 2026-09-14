package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalDouble;

@Service
public class SemanticSimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticSimilarityService.class);

    public static final double HIGH_SEMANTIC_THRESHOLD = 0.85;
    public static final double MODERATE_SEMANTIC_THRESHOLD = 0.65;

    private final EmbeddingService embeddingService;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public SemanticSimilarityService(EmbeddingService embeddingService,
            CosineSimilarityCalculator cosineSimilarityCalculator) {
        this.embeddingService = embeddingService;
        this.cosineSimilarityCalculator = cosineSimilarityCalculator != null ? cosineSimilarityCalculator : new CosineSimilarityCalculator();
    }

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

        try {
            if (embeddingService == null || !embeddingService.isAvailable()) {
                return OptionalDouble.empty();
            }

            Optional<float[]> vecA = embeddingService.getEmbedding(titleA);
            Optional<float[]> vecB = embeddingService.getEmbedding(titleB);

            if (vecA == null || vecA.isEmpty() || vecB == null || vecB.isEmpty()) {
                return OptionalDouble.empty();
            }

            double similarity = cosineSimilarityCalculator.calculateCosineSimilarity(vecA.get(), vecB.get());
            if (Double.isNaN(similarity) || Double.isInfinite(similarity)) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(similarity);
        } catch (Exception e) {
            logger.warn("Semantic similarity calculation encountered an error between '{}' and '{}': {}", titleA, titleB, e.getMessage());
            return OptionalDouble.empty();
        }
    }

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
        return embeddingService != null && embeddingService.isAvailable();
    }
}
