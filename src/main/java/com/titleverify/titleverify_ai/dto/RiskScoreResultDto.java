package com.titleverify.titleverify_ai.dto;

public class RiskScoreResultDto {

    private double riskScore;
    private String riskLevel;
    private double approvalConfidence;

    public RiskScoreResultDto() {
    }

    public RiskScoreResultDto(double riskScore, String riskLevel, double approvalConfidence) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.approvalConfidence = approvalConfidence;
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
}
