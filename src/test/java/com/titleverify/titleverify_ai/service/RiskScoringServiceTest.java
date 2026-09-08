package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RiskScoreResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {

    private RiskScoringService riskScoringService;

    @BeforeEach
    void setUp() {
        riskScoringService = new RiskScoringService(100.0, 30.0, 25.0, 25.0, 40.0, 15.0, 0.0);
    }

    @Test
    @DisplayName("Exact match should yield max risk score 100.0 and HIGH RISK level")
    void testExactMatchRiskScore() {
        RiskScoreResultDto result = riskScoringService.calculateRiskScore(true, 1.0, 1.0, 1.0, List.of());

        assertEquals(100.0, result.getRiskScore());
        assertEquals("HIGH RISK", result.getRiskLevel());
        assertEquals(0.0, result.getApprovalConfidence());
    }

    @Test
    @DisplayName("High similarity (0.9, 0.9, 0.9) should yield high risk score >= 70.0")
    void testHighSimilarityRiskScore() {
        RiskScoreResultDto result = riskScoringService.calculateRiskScore(false, 0.9, 0.9, 0.9, List.of());

        assertTrue(result.getRiskScore() >= 70.0, "Expected score >= 70.0, got: " + result.getRiskScore());
        assertEquals("HIGH RISK", result.getRiskLevel());
        assertTrue(result.getApprovalConfidence() <= 30.0);
    }

    @Test
    @DisplayName("Low similarity (0.1, 0.1, 0.1) with no rule violations should yield low risk score < 40.0")
    void testLowSimilarityRiskScore() {
        RiskScoreResultDto result = riskScoringService.calculateRiskScore(false, 0.1, 0.1, 0.1, List.of());

        assertTrue(result.getRiskScore() < 40.0, "Expected score < 40.0, got: " + result.getRiskScore());
        assertEquals("LOW RISK", result.getRiskLevel());
        assertTrue(result.getApprovalConfidence() > 60.0);
    }

    @Test
    @DisplayName("HIGH severity rule should add substantial penalty to risk score")
    void testRulePenaltyContribution() {
        RuleResultDto highRule = new RuleResultDto("R001", "Restricted Word", RuleSeverity.HIGH, true, "Restricted term matched", "crime", "Recommendation");

        RiskScoreResultDto resultWithoutRule = riskScoringService.calculateRiskScore(false, 0.2, 0.2, 0.2, List.of());
        RiskScoreResultDto resultWithRule = riskScoringService.calculateRiskScore(false, 0.2, 0.2, 0.2, List.of(highRule));

        assertTrue(resultWithRule.getRiskScore() >= resultWithoutRule.getRiskScore() + 40.0, "Rule penalty should increase score by 40");
    }

    @Test
    @DisplayName("Risk score should be safely bounded between 0.0 and 100.0")
    void testScoreBounds() {
        RuleResultDto highRule1 = new RuleResultDto("R1", "R1", RuleSeverity.HIGH, true, "msg", "ev", "rec");
        RuleResultDto highRule2 = new RuleResultDto("R2", "R2", RuleSeverity.HIGH, true, "msg", "ev", "rec");

        RiskScoreResultDto result = riskScoringService.calculateRiskScore(false, 1.0, 1.0, 1.0, List.of(highRule1, highRule2));
        assertTrue(result.getRiskScore() <= 100.0, "Risk score must be capped at 100.0");
        assertTrue(result.getApprovalConfidence() >= 0.0, "Approval confidence must not be negative");
    }
}
