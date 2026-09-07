package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RiskScoreResultDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Risk Scoring Engine Service.
 * Computes a normalized risk score (0.0 to 100.0) using configurable development weights.
 * Note: Development scoring configuration — not official PRGI criteria.
 */
@Service
public class RiskScoringService {

    private final double weightExactMatch;
    private final double weightFuzzy;
    private final double weightPhonetic;
    private final double weightSemantic;
    private final double penaltyHigh;
    private final double penaltyWarning;
    private final double penaltyInfo;

    public RiskScoringService(
            @Value("${titleverify.scoring.weight.exact-match:100.0}") double weightExactMatch,
            @Value("${titleverify.scoring.weight.fuzzy:30.0}") double weightFuzzy,
            @Value("${titleverify.scoring.weight.phonetic:25.0}") double weightPhonetic,
            @Value("${titleverify.scoring.weight.semantic:25.0}") double weightSemantic,
            @Value("${titleverify.scoring.rule-penalty.high:40.0}") double penaltyHigh,
            @Value("${titleverify.scoring.rule-penalty.warning:15.0}") double penaltyWarning,
            @Value("${titleverify.scoring.rule-penalty.info:0.0}") double penaltyInfo) {
        this.weightExactMatch = weightExactMatch;
        this.weightFuzzy = weightFuzzy;
        this.weightPhonetic = weightPhonetic;
        this.weightSemantic = weightSemantic;
        this.penaltyHigh = penaltyHigh;
        this.penaltyWarning = penaltyWarning;
        this.penaltyInfo = penaltyInfo;
    }

    public RiskScoreResultDto calculateRiskScore(boolean exactMatch,
                                                 Double fuzzyScore,
                                                 Double phoneticScore,
                                                 Double semanticScore,
                                                 List<RuleResultDto> ruleResults) {
        double rawScore;

        if (exactMatch) {
            rawScore = weightExactMatch;
        } else {
            double fScore = fuzzyScore != null ? Math.max(0.0, Math.min(1.0, fuzzyScore)) : 0.0;
            double pScore = phoneticScore != null ? Math.max(0.0, Math.min(1.0, phoneticScore)) : 0.0;
            double sScore = semanticScore != null ? Math.max(0.0, Math.min(1.0, semanticScore)) : 0.0;

            double fuzzyComponent = fScore * weightFuzzy;
            double phoneticComponent = pScore * weightPhonetic;
            double semanticComponent = sScore * weightSemantic;

            double rulePenalty = 0.0;
            if (ruleResults != null) {
                for (RuleResultDto rule : ruleResults) {
                    if (rule != null && rule.isTriggered()) {
                        if (rule.getSeverity() == RuleSeverity.HIGH) {
                            rulePenalty += penaltyHigh;
                        } else if (rule.getSeverity() == RuleSeverity.WARNING) {
                            rulePenalty += penaltyWarning;
                        } else if (rule.getSeverity() == RuleSeverity.INFO) {
                            rulePenalty += penaltyInfo;
                        }
                    }
                }
            }

            // Cap rule penalty accumulation to max 50.0
            rulePenalty = Math.min(50.0, rulePenalty);
            rawScore = fuzzyComponent + phoneticComponent + semanticComponent + rulePenalty;
        }

        double normalizedScore = roundToOneDecimal(Math.max(0.0, Math.min(100.0, rawScore)));
        double approvalConfidence = roundToOneDecimal(Math.max(0.0, Math.min(100.0, 100.0 - normalizedScore)));

        String riskLevel;
        if (normalizedScore >= 70.0) {
            riskLevel = "HIGH RISK";
        } else if (normalizedScore >= 40.0) {
            riskLevel = "MEDIUM RISK";
        } else {
            riskLevel = "LOW RISK";
        }

        return new RiskScoreResultDto(normalizedScore, riskLevel, approvalConfidence);
    }

    private double roundToOneDecimal(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
