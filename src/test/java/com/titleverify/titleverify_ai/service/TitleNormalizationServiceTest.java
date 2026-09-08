package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TitleNormalizationServiceTest {

    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
    }

    @Test
    @DisplayName("Should normalize uppercase to lowercase, trim extra spaces and strip punctuation")
    void testNormalize_UppercaseAndSpacesAndPunctuation() {
        String input = "  Namascar   DAILY  News!! ";
        String expected = "namascar daily news";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should normalize hyphenated and punctuated titles correctly")
    void testNormalize_PunctuationAndHyphens() {
        String input = "Karnataka-Herald! (Daily)";
        String expected = "karnataka herald daily";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should normalize English title correctly")
    void testNormalize_EnglishTitle() {
        String input = "Mysuru Green Agriculture";
        String expected = "mysuru green agriculture";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Regression test: Seeded Kannada title 'ನಮಸ್ಕಾರ ಹಿಂದೂ' should not return an empty string")
    void testNormalize_KannadaTitle_Regression() {
        String input = "ನಮಸ್ಕಾರ ಹಿಂದೂ";
        String actual = normalizationService.normalize(input);
        assertNotNull(actual);
        assertFalse(actual.isEmpty(), "Kannada title normalization must not return empty string");
        assertEquals("ನಮಸ್ಕಾರ ಹಿಂದೂ", actual);
    }

    @Test
    @DisplayName("Should normalize Hindi / Devanagari title correctly")
    void testNormalize_HindiTitle() {
        String input = "नमस्कार हिंदू";
        String actual = normalizationService.normalize(input);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals("नमस्कार हिंदू", actual);
    }

    @Test
    @DisplayName("Should normalize Telugu title correctly")
    void testNormalize_TeluguTitle() {
        String input = "నమస్కారం";
        String actual = normalizationService.normalize(input);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals("నమస్కారం", actual);
    }

    @Test
    @DisplayName("Should normalize Tamil title correctly")
    void testNormalize_TamilTitle() {
        String input = "வணக்கம்";
        String actual = normalizationService.normalize(input);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals("வணக்கம்", actual);
    }

    @Test
    @DisplayName("Should normalize Malayalam title correctly")
    void testNormalize_MalayalamTitle() {
        String input = "നമസ്കാരം";
        String actual = normalizationService.normalize(input);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals("നമസ്കാരം", actual);
    }

    @Test
    @DisplayName("Should handle extra spaces and punctuation in Indic titles")
    void testNormalize_IndicWithPunctuationAndExtraSpaces() {
        String input = "   ನಮಸ್ಕಾರ-  ಹಿಂದೂ!!  ";
        String expected = "ನಮಸ್ಕಾರ ಹಿಂದೂ";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should normalize mixed English and Kannada titles correctly")
    void testNormalize_MixedEnglishAndKannada() {
        String input = "Mysuru Voice (ಮೈಸೂರು ವಾಯ್ಸ್)";
        String expected = "mysuru voice ಮೈಸೂರು ವಾಯ್ಸ್";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should normalize mixed English and Hindi titles correctly")
    void testNormalize_MixedEnglishAndHindi() {
        String input = "India News (इंडिया न्यूज़)";
        String expected = "india news इंडिया न्यूज़";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should preserve numbers while stripping symbols")
    void testNormalize_NumbersAndSymbols() {
        String input = "No. 1 News #2024!";
        String expected = "no 1 news 2024";
        String actual = normalizationService.normalize(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return empty string for null input")
    void testNormalize_NullInput() {
        String actual = normalizationService.normalize(null);
        assertEquals("", actual);
    }

    @Test
    @DisplayName("Should return empty string for whitespace input")
    void testNormalize_BlankInput() {
        String actual = normalizationService.normalize("   ");
        assertEquals("", actual);
    }
}
