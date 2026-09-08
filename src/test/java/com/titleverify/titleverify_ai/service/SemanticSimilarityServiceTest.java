package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class SemanticSimilarityServiceTest {

    private TitleNormalizationService normalizationService;
    private EmbeddingService embeddingService;
    private CosineSimilarityCalculator cosineSimilarityCalculator;
    private SemanticSimilarityService semanticSimilarityService;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        embeddingService = new DevelopmentEmbeddingService(normalizationService);
        cosineSimilarityCalculator = new CosineSimilarityCalculator();
        semanticSimilarityService = new SemanticSimilarityService(embeddingService, cosineSimilarityCalculator);
    }

    @Test
    @DisplayName("Same meaning titles (Daily Evening News vs Evening Daily Bulletin) should yield high semantic similarity >= 0.85")
    void testCalculateSemanticSimilarity_SameMeaning() {
        OptionalDouble optScore = semanticSimilarityService.calculateSemanticSimilarity("Daily Evening News", "Evening Daily Bulletin");
        assertTrue(optScore.isPresent());
        double score = optScore.getAsDouble();
        assertTrue(score >= 0.85, "Expected high semantic similarity >= 0.85, got: " + score);
        assertEquals("HIGH", semanticSimilarityService.classifySemanticLevel(score));
    }

    @Test
    @DisplayName("Different meaning titles (Mysuru Chronicle vs Agriculture Weekly) should yield low semantic similarity < 0.65")
    void testCalculateSemanticSimilarity_DifferentMeaning() {
        OptionalDouble optScore = semanticSimilarityService.calculateSemanticSimilarity("Mysuru Chronicle", "Agriculture Weekly");
        assertTrue(optScore.isPresent());
        double score = optScore.getAsDouble();
        assertTrue(score < 0.65, "Expected low semantic similarity < 0.65, got: " + score);
        assertEquals("LOW", semanticSimilarityService.classifySemanticLevel(score));
    }

    @Test
    @DisplayName("Exact same title (India News vs India News) should yield 1.0 (100%) semantic similarity")
    void testCalculateSemanticSimilarity_ExactSameTitle() {
        OptionalDouble optScore = semanticSimilarityService.calculateSemanticSimilarity("India News", "India News");
        assertTrue(optScore.isPresent());
        assertEquals(1.0, optScore.getAsDouble(), 0.001);
        assertEquals("HIGH", semanticSimilarityService.classifySemanticLevel(optScore.getAsDouble()));
    }

    @Test
    @DisplayName("Short or empty inputs should be handled safely without exceptions")
    void testCalculateSemanticSimilarity_ShortOrEmptyInput() {
        assertDoesNotThrow(() -> {
            OptionalDouble opt1 = semanticSimilarityService.calculateSemanticSimilarity(null, "India News");
            assertTrue(opt1.isEmpty());

            OptionalDouble opt2 = semanticSimilarityService.calculateSemanticSimilarity("", "India News");
            assertTrue(opt2.isPresent());
            assertEquals(0.0, opt2.getAsDouble(), 0.001);

            OptionalDouble opt3 = semanticSimilarityService.calculateSemanticSimilarity("", "");
            assertTrue(opt3.isPresent());
            assertEquals(1.0, opt3.getAsDouble(), 0.001);
        });
    }
}
