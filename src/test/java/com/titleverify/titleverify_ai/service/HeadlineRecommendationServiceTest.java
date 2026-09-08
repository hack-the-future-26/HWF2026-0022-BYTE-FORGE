package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeadlineRecommendationServiceTest {

    private HeadlineRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new HeadlineRecommendationService();
    }

    @Test
    void testRecommendsHeadlineWithBestBalanceOfRelevanceAndSafety() {
        // Option 1: High Relevance (85%), Low Risk (Score 10.0, ACCEPT)
        TitleVerificationResultDto v1 = new TitleVerificationResultDto();
        v1.setFinalDecision("ACCEPT");
        v1.setRiskScore(10.0);
        v1.setRiskLevel("LOW RISK");
        v1.setApprovalConfidence(90.0);
        HeadlineVerificationResultDto h1 = new HeadlineVerificationResultDto("Karnataka Tech Express", 85.0, v1);

        // Option 2: Low Relevance (40%), Low Risk (Score 5.0, ACCEPT) -> safe but irrelevant!
        TitleVerificationResultDto v2 = new TitleVerificationResultDto();
        v2.setFinalDecision("ACCEPT");
        v2.setRiskScore(5.0);
        v2.setRiskLevel("LOW RISK");
        v2.setApprovalConfidence(95.0);
        HeadlineVerificationResultDto h2 = new HeadlineVerificationResultDto("Deccan General News", 40.0, v2);

        // Option 3: High Relevance (90%), High Risk (Score 80.0, HIGH RISK) -> relevant but unsafe!
        TitleVerificationResultDto v3 = new TitleVerificationResultDto();
        v3.setFinalDecision("HIGH RISK");
        v3.setRiskScore(80.0);
        v3.setRiskLevel("HIGH RISK");
        v3.setApprovalConfidence(20.0);
        v3.setRuleResults(List.of(new RuleResultDto("PROHIBITED_WORD", "Prohibited Word Rule", RuleSeverity.HIGH, true, "Matches police", "police", "Change title")));
        HeadlineVerificationResultDto h3 = new HeadlineVerificationResultDto("Police Special Bulletin", 90.0, v3);

        HeadlineVerificationResultDto best = recommendationService.selectBestHeadline(List.of(h1, h2, h3));

        assertNotNull(best);
        assertEquals("Karnataka Tech Express", best.getGeneratedHeadline(), "Should recommend Option 1 due to high relevance AND high safety!");
    }

    @Test
    void testDeterministicTieBreaking() {
        TitleVerificationResultDto v1 = new TitleVerificationResultDto();
        v1.setFinalDecision("ACCEPT");
        v1.setRiskScore(10.0);
        HeadlineVerificationResultDto h1 = new HeadlineVerificationResultDto("Title Option A", 80.0, v1);

        TitleVerificationResultDto v2 = new TitleVerificationResultDto();
        v2.setFinalDecision("ACCEPT");
        v2.setRiskScore(10.0);
        HeadlineVerificationResultDto h2 = new HeadlineVerificationResultDto("Title Option B", 80.0, v2);

        HeadlineVerificationResultDto best = recommendationService.selectBestHeadline(List.of(h1, h2));

        assertNotNull(best);
        assertEquals("Title Option A", best.getGeneratedHeadline());
    }

    @Test
    void testGenerateRecommendationExplanationFormat() {
        TitleVerificationResultDto v = new TitleVerificationResultDto();
        v.setFinalDecision("ACCEPT");
        v.setRiskScore(12.5);
        v.setClosestMatch("Deccan Chronicle");
        HeadlineVerificationResultDto h = new HeadlineVerificationResultDto("Deccan Tech Times", 88.0, v);

        String explanation = recommendationService.generateRecommendationExplanation(h);
        assertTrue(explanation.contains("88.0% relevance"));
        assertTrue(explanation.contains("Pre-screening recommendation"));
        assertTrue(explanation.contains("ACCEPT"));
        assertTrue(explanation.contains("Deccan Chronicle"));
    }
}
