package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneticSimilarityServiceTest {

    private TitleNormalizationService normalizationService;
    private FuzzySimilarityService fuzzySimilarityService;
    private PhoneticSimilarityService phoneticSimilarityService;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        fuzzySimilarityService = new FuzzySimilarityService();
        phoneticSimilarityService = new PhoneticSimilarityService(normalizationService, fuzzySimilarityService);
    }

    @Test
    @DisplayName("Same pronunciation/similar spelling (Color News vs Colour News) should yield high phonetic similarity >= 0.85")
    void testCalculatePhoneticSimilarity_ColorVsColour() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("Color News", "Colour News");
        assertTrue(score >= 0.85, "Expected high phonetic similarity >= 0.85, got: " + score);
        assertEquals("HIGH", phoneticSimilarityService.classifyPhoneticLevel(score));
    }

    @Test
    @DisplayName("Different spelling but similar pronunciation (Namascar News vs Namaskar News) should yield high phonetic similarity >= 0.85")
    void testCalculatePhoneticSimilarity_NamascarVsNamaskar() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("Namascar News", "Namaskar News");
        assertTrue(score >= 0.85, "Expected high phonetic similarity >= 0.85, got: " + score);
        assertEquals("HIGH", phoneticSimilarityService.classifyPhoneticLevel(score));
    }

    @Test
    @DisplayName("Exact same title (India News vs India News) should yield 1.0 (100%) phonetic similarity")
    void testCalculatePhoneticSimilarity_ExactSameTitle() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("India News", "India News");
        assertEquals(1.0, score, 0.001);
        assertEquals("HIGH", phoneticSimilarityService.classifyPhoneticLevel(score));
    }

    @Test
    @DisplayName("Clearly different words (Mysuru Chronicle vs Agriculture Weekly) should yield low phonetic similarity < 0.65")
    void testCalculatePhoneticSimilarity_ClearlyDifferentWords() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("Mysuru Chronicle", "Agriculture Weekly");
        assertTrue(score < 0.65, "Expected low phonetic similarity < 0.65, got: " + score);
        assertEquals("LOW", phoneticSimilarityService.classifyPhoneticLevel(score));
    }

    @Test
    @DisplayName("Multi-word titles (Colour Daily News vs Color Daily News) should process word-level phonetics consistently")
    void testCalculatePhoneticSimilarity_MultiWordTitle() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("Colour Daily News", "Color Daily News");
        assertTrue(score >= 0.85, "Expected high phonetic similarity for multi-word title, got: " + score);
        String encodedA = phoneticSimilarityService.encodeTitleToPhonetic("Colour Daily News");
        String encodedB = phoneticSimilarityService.encodeTitleToPhonetic("Color Daily News");
        assertEquals(encodedA, encodedB);
    }

    @Test
    @DisplayName("Non-Latin scripts (Kannada, Hindi) should handle empty Metaphone encoding safely returning 0.0 phonetic score")
    void testCalculatePhoneticSimilarity_NonLatinScriptsSafelyHandled() {
        double score = phoneticSimilarityService.calculatePhoneticSimilarity("ನಮಸ್ಕಾರ ಹಿಂದೂ", "ವಿಜಯವಾಣಿ");
        assertEquals(0.0, score, 0.001);
        assertEquals("LOW", phoneticSimilarityService.classifyPhoneticLevel(score));
    }

    @Test
    @DisplayName("Empty or null inputs should be handled safely without exceptions")
    void testCalculatePhoneticSimilarity_EmptyAndNullInput() {
        assertDoesNotThrow(() -> {
            assertEquals(0.0, phoneticSimilarityService.calculatePhoneticSimilarity(null, "India News"));
            assertEquals(0.0, phoneticSimilarityService.calculatePhoneticSimilarity("India News", null));
            assertEquals(0.0, phoneticSimilarityService.calculatePhoneticSimilarity(null, null));
            assertEquals(0.0, phoneticSimilarityService.calculatePhoneticSimilarity("", "India News"));
            assertEquals(1.0, phoneticSimilarityService.calculatePhoneticSimilarity("", ""));
        });
    }
}
