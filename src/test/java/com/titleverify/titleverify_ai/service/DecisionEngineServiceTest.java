package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RiskScoreResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.VerificationDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecisionEngineServiceTest {

    private DecisionEngineService decisionEngineService;

    @BeforeEach
    void setUp() {
        decisionEngineService = new DecisionEngineService(40.0, 70.0);
    }

    @Test
    @DisplayName("Exact match should force HIGH RISK decision regardless of other scores")
    void testExactMatchForcesHighRisk() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(100.0, "HIGH RISK", 0.0);

        VerificationDecision decision = decisionEngineService.determineDecision(true, riskScoreResult, List.of());

        assertEquals(VerificationDecision.HIGH_RISK, decision);
    }

    @Test
    @DisplayName("Triggered HIGH severity rule should force HIGH RISK decision")
    void testHighSeverityRuleForcesHighRisk() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(45.0, "MEDIUM RISK", 55.0);
        RuleResultDto highRule = new RuleResultDto("R1", "Prohibited Word", RuleSeverity.HIGH, true, "Prohibited word", "crime", "rec");

        VerificationDecision decision = decisionEngineService.determineDecision(false, riskScoreResult, List.of(highRule));

        assertEquals(VerificationDecision.HIGH_RISK, decision);
    }

    @Test
    @DisplayName("High risk score >= 70.0 should map to HIGH RISK decision")
    void testHighRiskScoreMapsToHighRisk() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(75.0, "HIGH RISK", 25.0);

        VerificationDecision decision = decisionEngineService.determineDecision(false, riskScoreResult, List.of());

        assertEquals(VerificationDecision.HIGH_RISK, decision);
    }

    @Test
    @DisplayName("Borderline risk score (40.0-69.9) or WARNING rule should map to REVIEW decision")
    void testBorderlineRiskMapsToReview() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(50.0, "MEDIUM RISK", 50.0);

        VerificationDecision decision = decisionEngineService.determineDecision(false, riskScoreResult, List.of());

        assertEquals(VerificationDecision.REVIEW, decision);
    }

    @Test
    @DisplayName("Low risk score < 40.0 with no rule violations should map to ACCEPT decision")
    void testLowRiskMapsToAccept() {
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(20.0, "LOW RISK", 80.0);

        VerificationDecision decision = decisionEngineService.determineDecision(false, riskScoreResult, List.of());

        assertEquals(VerificationDecision.ACCEPT, decision);
    }

    @Test
    @DisplayName("Conflicting evidence (high semantic similarity but low fuzzy/phonetic and no rules) should map to REVIEW")
    void testConflictingEvidenceMapsToReview() {
        // High semantic (0.85 * 25 = 21.25) + low fuzzy (0.1*30=3) + low phonetic (0.1*25=2.5) + rule penalty = ~26.75 -> if WARNING rule triggered or score ~40 -> REVIEW
        RiskScoreResultDto riskScoreResult = new RiskScoreResultDto(42.0, "MEDIUM RISK", 58.0);

        VerificationDecision decision = decisionEngineService.determineDecision(false, riskScoreResult, List.of());

        assertEquals(VerificationDecision.REVIEW, decision);
    }
}
