package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RiskScoreResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.VerificationDecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DecisionEngineService {

    private final double thresholdAccept;
    private final double thresholdHighRisk;

    public DecisionEngineService(
            @Value("${titleverify.decision.threshold.accept:40.0}") double thresholdAccept,
            @Value("${titleverify.decision.threshold.high-risk:70.0}") double thresholdHighRisk) {
        this.thresholdAccept = thresholdAccept;
        this.thresholdHighRisk = thresholdHighRisk;
    }

    public VerificationDecision determineDecision(boolean exactMatch,
            RiskScoreResultDto riskScoreResult,
            List<RuleResultDto> ruleResults) {
        // Safety Override 1: Exact Title Match -> HIGH RISK
        if (exactMatch) {
            return VerificationDecision.HIGH_RISK;
        }

        // Safety Override 2: Triggered HIGH severity rule -> HIGH RISK
        if (hasTriggeredRuleWithSeverity(ruleResults, RuleSeverity.HIGH)) {
            return VerificationDecision.HIGH_RISK;
        }

        double score = riskScoreResult != null ? riskScoreResult.getRiskScore() : 0.0;

        // High Risk Threshold
        if (score >= thresholdHighRisk) {
            return VerificationDecision.HIGH_RISK;
        }

        // Borderline / Mixed Evidence -> REVIEW
        if (score >= thresholdAccept || hasTriggeredRuleWithSeverity(ruleResults, RuleSeverity.WARNING)) {
            return VerificationDecision.REVIEW;
        }

        // Low Risk -> ACCEPT
        return VerificationDecision.ACCEPT;
    }

    private boolean hasTriggeredRuleWithSeverity(List<RuleResultDto> ruleResults, RuleSeverity severity) {
        if (ruleResults == null) {
            return false;
        }
        return ruleResults.stream()
                .anyMatch(r -> r != null && r.isTriggered() && r.getSeverity() == severity);
    }
}
