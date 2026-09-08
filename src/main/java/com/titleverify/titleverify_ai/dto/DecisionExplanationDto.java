package com.titleverify.titleverify_ai.dto;

import java.util.List;

public class DecisionExplanationDto {

    private String finalDecision;
    private double riskScore;
    private String riskLevel;
    private double approvalConfidence;
    private String closestMatch;
    private List<String> reasons;
    private String recommendation;

    public DecisionExplanationDto() {
    }

    public DecisionExplanationDto(String finalDecision, double riskScore, String riskLevel,
                                  double approvalConfidence, String closestMatch,
                                  List<String> reasons, String recommendation) {
        this.finalDecision = finalDecision;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.approvalConfidence = approvalConfidence;
        this.closestMatch = closestMatch;
        this.reasons = reasons;
        this.recommendation = recommendation;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getApprovalConfidence() {
        return approvalConfidence;
    }

    public void setApprovalConfidence(double approvalConfidence) {
        this.approvalConfidence = approvalConfidence;
    }

    public String getClosestMatch() {
        return closestMatch;
    }

    public void setClosestMatch(String closestMatch) {
        this.closestMatch = closestMatch;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
