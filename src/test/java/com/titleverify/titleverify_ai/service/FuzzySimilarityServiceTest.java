package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuzzySimilarityServiceTest {

    private FuzzySimilarityService fuzzySimilarityService;

    @BeforeEach
    void setUp() {
        fuzzySimilarityService = new FuzzySimilarityService();
    }

    @Test
    @DisplayName("Exact text should yield 1.0 (100%) similarity score")
    void testCalculateSimilarity_ExactText() {
        double score = fuzzySimilarityService.calculateSimilarity("India News", "India News");
        assertEquals(1.0, score, 0.001);
        assertEquals("HIGH", fuzzySimilarityService.classifySimilarityLevel(score));
    }

    @Test
    @DisplayName("Very similar spelling (Namaskar News vs Namascar News) should yield high similarity >= 0.85")
    void testCalculateSimilarity_VerySimilarSpelling() {
        double score = fuzzySimilarityService.calculateSimilarity("Namaskar News", "Namascar News");
        assertTrue(score >= 0.85, "Expected high similarity >= 0.85, got: " + score);
        assertEquals("HIGH", fuzzySimilarityService.classifySimilarityLevel(score));
    }

    @Test
    @DisplayName("Moderately similar titles (Mysuru Chronicle vs Mysore Chronicle) should yield moderate similarity")
    void testCalculateSimilarity_ModeratelySimilar() {
        double score = fuzzySimilarityService.calculateSimilarity("Mysuru Chronicle", "Mysore Chronicle");
        assertTrue(score >= 0.65 && score < 1.0, "Expected moderate/high similarity, got: " + score);
        assertTrue(score >= 0.65);
    }

    @Test
    @DisplayName("Very different titles (Mysuru Chronicle vs Agriculture Weekly) should yield low similarity < 0.65")
    void testCalculateSimilarity_VeryDifferent() {
        double score = fuzzySimilarityService.calculateSimilarity("Mysuru Chronicle", "Agriculture Weekly");
        assertTrue(score < 0.65, "Expected low similarity < 0.65, got: " + score);
        assertEquals("LOW", fuzzySimilarityService.classifySimilarityLevel(score));
    }

    @Test
    @DisplayName("Empty or null inputs should be handled safely without exceptions")
    void testCalculateSimilarity_EmptyAndNullInput() {
        assertDoesNotThrow(() -> {
            assertEquals(0.0, fuzzySimilarityService.calculateSimilarity(null, "India News"));
            assertEquals(0.0, fuzzySimilarityService.calculateSimilarity("India News", null));
            assertEquals(0.0, fuzzySimilarityService.calculateSimilarity(null, null));
            assertEquals(0.0, fuzzySimilarityService.calculateSimilarity("", "India News"));
            assertEquals(1.0, fuzzySimilarityService.calculateSimilarity("", ""));
        });
    }

    @Test
    @DisplayName("Should correctly calculate Levenshtein distance")
    void testCalculateLevenshteinDistance() {
        assertEquals(0, fuzzySimilarityService.calculateLevenshteinDistance("test", "test"));
        assertEquals(1, fuzzySimilarityService.calculateLevenshteinDistance("cat", "hat"));
        assertEquals(1, fuzzySimilarityService.calculateLevenshteinDistance("namaskar", "namascar"));
    }
}
