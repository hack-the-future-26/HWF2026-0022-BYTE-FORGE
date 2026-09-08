package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentRelevanceServiceTest {

    private ContentRelevanceService relevanceService;

    @BeforeEach
    void setUp() {
        relevanceService = new ContentRelevanceService();
    }

    @Test
    void testHighRelevanceWhenHeadlineMatchesArticleKeywords() {
        String article = "Bengaluru Tech Summit 2026 inaugurated today with over 500 tech startups participating in Artificial Intelligence and Renewable Energy exhibitions.";
        String headline = "Bengaluru Tech Summit 2026 Inaugurated";

        double score = relevanceService.calculateRelevance(headline, article);
        assertTrue(score >= 70.0, "Score should be high for keyword match: " + score);
        assertEquals("HIGH RELEVANCE", relevanceService.classifyRelevanceLevel(score));
    }

    @Test
    void testLowRelevanceWhenHeadlineHasUnrelatedKeywords() {
        String article = "Bengaluru Tech Summit 2026 inaugurated today with over 500 tech startups participating.";
        String headline = "Stock Market Reaches All Time High In Mumbai";

        double score = relevanceService.calculateRelevance(headline, article);
        assertTrue(score <= 50.0, "Score should be low for unrelated content: " + score);
    }

    @Test
    void testNullOrBlankInputsReturnZero() {
        assertEquals(0.0, relevanceService.calculateRelevance(null, "Article text"));
        assertEquals(0.0, relevanceService.calculateRelevance("Headline", null));
        assertEquals(0.0, relevanceService.calculateRelevance("", ""));
    }
}
