git status 
package com.titleverify.titleverify_ai.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CosineSimilarityCalculatorTest {

    private CosineSimilarityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CosineSimilarityCalculator();
    }

    @Test
    @DisplayName("Identical vectors should yield 1.0 cosine similarity")
    void testCalculateCosineSimilarity_IdenticalVectors() {
        float[] vectorA = new float[]{0.5f, 0.5f, 0.5f, 0.5f};
        float[] vectorB = new float[]{0.5f, 0.5f, 0.5f, 0.5f};

        double result = calculator.calculateCosineSimilarity(vectorA, vectorB);
        assertEquals(1.0, result, 0.001);
    }

    @Test
    @DisplayName("Orthogonal vectors should yield 0.0 cosine similarity")
    void testCalculateCosineSimilarity_OrthogonalVectors() {
        float[] vectorA = new float[]{1.0f, 0.0f, 0.0f};
        float[] vectorB = new float[]{0.0f, 1.0f, 0.0f};

        double result = calculator.calculateCosineSimilarity(vectorA, vectorB);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    @DisplayName("Zero vectors should safely yield 0.0 cosine similarity")
    void testCalculateCosineSimilarity_ZeroVectors() {
        float[] vectorA = new float[]{0.0f, 0.0f, 0.0f};
        float[] vectorB = new float[]{0.0f, 0.0f, 0.0f};

        double result = calculator.calculateCosineSimilarity(vectorA, vectorB);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    @DisplayName("Mismatched vector lengths should safely yield 0.0 cosine similarity")
    void testCalculateCosineSimilarity_InvalidMismatchedLengths() {
        float[] vectorA = new float[]{1.0f, 2.0f};
        float[] vectorB = new float[]{1.0f, 2.0f, 3.0f};

        double result = calculator.calculateCosineSimilarity(vectorA, vectorB);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    @DisplayName("Null input vectors should safely yield 0.0 cosine similarity")
    void testCalculateCosineSimilarity_NullVectors() {
        assertEquals(0.0, calculator.calculateCosineSimilarity(null, new float[]{1.0f}));
        assertEquals(0.0, calculator.calculateCosineSimilarity(new float[]{1.0f}, null));
        assertEquals(0.0, calculator.calculateCosineSimilarity(null, null));
    }
}
