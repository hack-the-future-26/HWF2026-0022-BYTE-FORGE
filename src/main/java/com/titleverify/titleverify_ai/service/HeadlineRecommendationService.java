package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class HeadlineRecommendationService {

    /**
     * Selects the recommended headline from the list of generated and verified headlines
     * using a transparent, deterministic multi-dimensional ranking algorithm.
     *
     * Selection Criteria:
     * 1. Balance content relevance and title-clearance safety.
     * 2. Prefer ACCEPT decision over REVIEW, and REVIEW over HIGH RISK.
       3. Among comparable decisions, prefer higher content relevance and lower risk score.
     * 4. Consider severe rule findings as a major penalty factor.
     */
    public HeadlineVerificationResultDto selectBestHeadline(List<HeadlineVerificationResultDto> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        return results.stream()
                .max(Comparator.comparingDouble(this::calculateSuitabilityScore)
                        .thenComparingDouble(HeadlineVerificationResultDto::getContentRelevance)
                        .thenComparingDouble(r -> r.getVerificationResult() != null ? -r.getVerificationResult().getRiskScore() : -100.0))
                .orElse(results.get(0));
    }

    /**
     * Transparent Suitability Score Calculation:
     * Suitability = (0.7 * Content Relevance %) + (0.3 * Verification Safety %) + Decision Tier Bonus - Rule Penalties
     */
    public double calculateSuitabilityScore(HeadlineVerificationResultDto item) {
        if (item.isVerificationError() || item.getVerificationResult() == null) {
            return -200.0; // Unverified titles rank below validly verified titles
        }

        double contentRelevance = item.getContentRelevance();
        TitleVerificationResultDto vResult = item.getVerificationResult();
        double riskScore = vResult.getRiskScore() != null ? vResult.getRiskScore() : 50.0;
        double verificationSafety = Math.max(0.0, 100.0 - riskScore);

        // 1. Base Weighted Score: 70% Content Relevance + 30% Verification Safety
        double baseSuitability = (0.7 * contentRelevance) + (0.3 * verificationSafety);

        // 2. Decision Tier Modifier
        double decisionModifier = 0.0;
        String decision = vResult.getFinalDecision();
        if ("ACCEPT".equalsIgnoreCase(decision)) {
            decisionModifier += 50.0;
        } else if ("REVIEW".equalsIgnoreCase(decision)) {
            decisionModifier += 0.0;
        } else if ("HIGH RISK".equalsIgnoreCase(decision) || "HIGH_RISK".equalsIgnoreCase(decision)) {
            decisionModifier -= 100.0;
        }

        // 3. High Severity Rule Penalty
        double rulePenalty = 0.0;
        if (vResult.getRuleResults() != null) {
            for (RuleResultDto rule : vResult.getRuleResults()) {
                if (rule.isTriggered() && rule.getSeverity() == RuleSeverity.HIGH) {
                    rulePenalty += 50.0;
                }
            }
        }

        return baseSuitability + decisionModifier - rulePenalty;
    }

    /**
     * Generates a human-readable, deterministic explanation of why this headline was selected as recommended.
     */
    public String generateRecommendationExplanation(HeadlineVerificationResultDto recommended) {
        if (recommended == null) {
            return "No headline could be recommended.";
        }

        if (recommended.isVerificationError() || recommended.getVerificationResult() == null) {
            return "Pre-screening recommendation based on content alignment, but title verification could not be completed.";
        }

        TitleVerificationResultDto v = recommended.getVerificationResult();
        double relevance = recommended.getContentRelevance();
        double riskScore = v.getRiskScore() != null ? v.getRiskScore() : 0.0;
        String decision = v.getFinalDecision();
        String closest = v.getClosestMatch();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pre-screening recommendation: Selected for strong content alignment (%.1f%% relevance) to your news story ", relevance));

        if ("ACCEPT".equalsIgnoreCase(decision)) {
            sb.append(String.format("and favorable title-verification clearance (Risk Score: %.1f/100, Decision: ACCEPT). ", riskScore));
        } else if ("REVIEW".equalsIgnoreCase(decision)) {
            sb.append(String.format("and moderate clearance risk (Risk Score: %.1f/100, Decision: REVIEW). ", riskScore));
        } else {
            sb.append(String.format("despite title clearance warnings (Risk Score: %.1f/100, Decision: %s). ", riskScore, decision));
        }

        if (closest != null && !closest.isBlank() && !"No comparable registered title found".equalsIgnoreCase(closest)) {
            sb.append("Closest registered title evaluated: '").append(closest).append("'. ");
        } else {
            sb.append("No direct conflicting registered title found. ");
        }

        sb.append("Provides the best balance between story representation and title-registration safety among generated options.");
        return sb.toString();
    }
}
