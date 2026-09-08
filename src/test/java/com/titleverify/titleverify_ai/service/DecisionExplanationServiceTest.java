package com.titleverify.titleverify_ai.service;


import com.titleverify.titleverify_ai.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecisionExplanationServiceTest {

    private DecisionExplanationService explanationService;

    @BeforeEach
    void setUp() {
        explanationService = new DecisionExplanationService();
    }

    @Test
    @DisplayName("Should generate deterministic explanation for HIGH RISK decision with exact match and rule findings")
    void testHighRiskExplanationGeneration() {
        RuleResultDto rule = new RuleResultDto("R1", "Prohibited Word", RuleSeverity.HIGH, true, "Restricted word 'crime' detected", "crime", "rec");
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(100.0, "HIGH RISK", 0.0);

        DecisionExplanationDto explanation = explanationService.generateExplanation(
                "Crime News", true, "Crime News", "Crime News", 1.0, 1.0, 1.0,
                List.of(rule), riskScoreResult, VerificationDecision.HIGH_RISK
        );

        assertEquals("HIGH RISK", explanation.getFinalDecision());
        assertEquals(100.0, explanation.getRiskScore());
        assertEquals("HIGH RISK", explanation.getRiskLevel());
        assertEquals(0.0, explanation.getApprovalConfidence());
        assertEquals("Crime News", explanation.getClosestMatch());
        assertNotNull(explanation.getReasons());
        assertFalse(explanation.getReasons().isEmpty());
        assertTrue(explanation.getReasons().stream().anyMatch(r -> r.contains("Exact match")));
        assertTrue(explanation.getReasons().stream().anyMatch(r -> r.contains("Rule Triggered")));
        assertNotNull(explanation.getRecommendation());
    }

    @Test
    @DisplayName("Same evidence should produce exact identical deterministic explanation")
    void testDeterministicExplanationReproducibility() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(25.0, "LOW RISK", 75.0);

        DecisionExplanationDto exp1 = explanationService.generateExplanation(
                "Morning Times", false, null, "Agriculture Weekly", 0.2, 0.2, 0.2,
                List.of(), riskScoreResult, VerificationDecision.ACCEPT
        );

        DecisionExplanationDto exp2 = explanationService.generateExplanation(
                "Morning Times", false, null, "Agriculture Weekly", 0.2, 0.2, 0.2,
                List.of(), riskScoreResult, VerificationDecision.ACCEPT
        );

        assertEquals(exp1.getFinalDecision(), exp2.getFinalDecision());
        assertEquals(exp1.getRiskScore(), exp2.getRiskScore());
        assertEquals(exp1.getReasons(), exp2.getReasons());
        assertEquals(exp1.getRecommendation(), exp2.getRecommendation());
    }
}
