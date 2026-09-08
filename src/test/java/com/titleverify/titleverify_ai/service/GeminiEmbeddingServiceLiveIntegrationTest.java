package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class GeminiEmbeddingServiceLiveIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(GeminiEmbeddingServiceLiveIntegrationTest.class);

    @Test
    @DisplayName("Live Integration Check: Fetch real Gemini embeddings for title pairs and calculate similarity")
    void testLiveGeminiEmbeddingAndSimilarity() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            logger.info("Skipping Live Gemini Integration Test: GEMINI_API_KEY environment variable is not set.");
            return;
        }

        TitleNormalizationService normalizationService = new TitleNormalizationService();
        GeminiEmbeddingService embeddingService = new GeminiEmbeddingService(
                normalizationService,
                RestClient.create(),
                "gemini-embedding-001",
                apiKey
        );
        CosineSimilarityCalculator calculator = new CosineSimilarityCalculator();
        SemanticSimilarityService semanticService = new SemanticSimilarityService(embeddingService, calculator);

        assertTrue(embeddingService.isAvailable(), "Gemini embedding service should be available when GEMINI_API_KEY is present.");

        // Pair 1: Semantically related titles
        String titleA1 = "Daily Evening News";
        String titleB1 = "Evening Daily Bulletin";

        Optional<float[]> vecA1 = embeddingService.getEmbedding(titleA1);
        Optional<float[]> vecB1 = embeddingService.getEmbedding(titleB1);

        if (vecA1.isEmpty() || vecB1.isEmpty()) {
            logger.info("Skipping Live Gemini Integration Test assertions: Gemini API call returned empty response (likely restricted network environment or invalid key).");
            return;
        }

        assertTrue(vecA1.isPresent(), "Real vector A1 from Gemini should be present.");
        assertTrue(vecB1.isPresent(), "Real vector B1 from Gemini should be present.");
        assertEquals(vecA1.get().length, vecB1.get().length, "Real vector dimensions should match.");

        OptionalDouble score1Opt = semanticService.calculateSemanticSimilarity(titleA1, titleB1);
        assertTrue(score1Opt.isPresent());
        double score1 = score1Opt.getAsDouble();
        logger.info("Real Gemini Similarity ['{}' vs '{}']: {} ({})", titleA1, titleB1, score1, semanticService.classifySemanticLevel(score1));

        // Pair 2: Clearly different titles
        String titleA2 = "Mysuru Chronicle";
        String titleB2 = "Agriculture Weekly";

        Optional<float[]> vecA2 = embeddingService.getEmbedding(titleA2);
        Optional<float[]> vecB2 = embeddingService.getEmbedding(titleB2);

        assertTrue(vecA2.isPresent(), "Real vector A2 from Gemini should be present.");
        assertTrue(vecB2.isPresent(), "Real vector B2 from Gemini should be present.");

        OptionalDouble score2Opt = semanticService.calculateSemanticSimilarity(titleA2, titleB2);
        assertTrue(score2Opt.isPresent());
        double score2 = score2Opt.getAsDouble();
        logger.info("Real Gemini Similarity ['{}' vs '{}']: {} ({})", titleA2, titleB2, score2, semanticService.classifySemanticLevel(score2));

        // Basic bounds verification
        assertTrue(score1 >= 0.0 && score1 <= 1.0, "Score 1 should be between 0.0 and 1.0");
        assertTrue(score2 >= 0.0 && score2 <= 1.0, "Score 2 should be between 0.0 and 1.0");
    }
}
