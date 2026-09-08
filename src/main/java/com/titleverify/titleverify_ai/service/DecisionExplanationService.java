package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Decision Explanation Service.
 * Deterministically constructs structured explanations, reason summaries, and recommendations based on verification evidence.
 */
@Service
public class DecisionExplanationService {

    public DecisionExplanationDto generateExplanation(String proposedTitle,
                                                      boolean exactMatch,
                                                      String matchedTitle,
                                                      String closestCandidate,
                                                      Double fuzzyScore,
                                                      Double phoneticScore,
                                                      Double semanticScore,
                                                      List<RuleResultDto> ruleResults,
                                                      RiskScoreResultDto riskScoreResult,
                                                      VerificationDecision decision) {
        List<String> reasons = new ArrayList<>();

        if (exactMatch) {
            reasons.add("Exact match found with existing registered title: '" + matchedTitle + "'.");
        }

        if (closestCandidate != null && !closestCandidate.trim().isEmpty()) {
            if (fuzzyScore != null && fuzzyScore >= 0.70) {
                reasons.add(String.format("High string (fuzzy) similarity detected against '%s' (%.0f%%).", closestCandidate, fuzzyScore * 100));
            }
            if (phoneticScore != null && phoneticScore >= 0.70) {
                reasons.add(String.format("High phonetic (soundex) similarity detected against '%s' (%.0f%%).", closestCandidate, phoneticScore * 100));
            }
            if (semanticScore != null && semanticScore >= 0.70) {
                reasons.add(String.format("High semantic vector similarity detected against '%s' (%.0f%%).", closestCandidate, semanticScore * 100));
            }
        }

        if (ruleResults != null) {
            for (RuleResultDto rule : ruleResults) {
                if (rule != null && rule.isTriggered()) {
                    reasons.add(String.format("Rule Triggered [%s]: %s", rule.getRuleName(), rule.getMessage()));
                }
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("No significant title conflict, similarity overlap, or rule violation detected.");
        }

        String recommendation;
        if (decision == VerificationDecision.HIGH_RISK) {
            recommendation = "Strong title similarity or serious rule conflict detected. Choose a substantially different core title before official submission.";
        } else if (decision == VerificationDecision.REVIEW) {
            recommendation = "Borderline similarity or potential rule overlap detected. Review closest candidates and consider modifying generic affixes or periodicity terms.";
        } else {
            recommendation = "Title demonstrates low verification risk under automated pre-screening criteria.";
        }

        double riskScore = riskScoreResult != null ? riskScoreResult.getRiskScore() : 0.0;
        String riskLevel = riskScoreResult != null ? riskScoreResult.getRiskLevel() : "LOW RISK";
        double approvalConfidence = riskScoreResult != null ? riskScoreResult.getApprovalConfidence() : 100.0;

        return new DecisionExplanationDto(
                decision.getCode(),
                riskScore,
                riskLevel,
                approvalConfidence,
                closestCandidate != null ? closestCandidate : matchedTitle,
                reasons,
                recommendation
        );
    }
}
