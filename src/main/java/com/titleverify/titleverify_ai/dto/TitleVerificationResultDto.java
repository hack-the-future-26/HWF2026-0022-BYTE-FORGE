package com.titleverify.titleverify_ai.dto;

import java.util.List;

public class TitleVerificationResultDto {

    private String proposedTitle;
    private String normalizedTitle;
    private boolean exactMatch;
    private String matchedTitle;
    private List<String> candidateTitles;
    private String decisionStage;
    private List<CandidateMatchDto> candidateMatches;
    private Double highestFuzzySimilarity;
    private String topFuzzyMatch;
    private Double highestPhoneticSimilarity;
    private String topPhoneticMatch;
    private Double highestSemanticSimilarity;
    private String topSemanticMatch;
    private List<RuleResultDto> ruleResults;
    private String finalDecision;
    private Double riskScore;
    private String riskLevel;
    private Double approvalConfidence;
    private String closestMatch;
    private List<String> reasons;
    private String recommendation;

    public TitleVerificationResultDto() {
    }

    public TitleVerificationResultDto(String proposedTitle, String normalizedTitle, boolean exactMatch,
                                      String matchedTitle, List<String> candidateTitles, String decisionStage) {
        this.proposedTitle = proposedTitle;
        this.normalizedTitle = normalizedTitle;
        this.exactMatch = exactMatch;
        this.matchedTitle = matchedTitle;
        this.candidateTitles = candidateTitles;
        this.decisionStage = decisionStage;
    }

    public TitleVerificationResultDto(String proposedTitle, String normalizedTitle, boolean exactMatch,
                                      String matchedTitle, List<String> candidateTitles, String decisionStage,
                                      List<CandidateMatchDto> candidateMatches, Double highestFuzzySimilarity,
                                      String topFuzzyMatch, Double highestPhoneticSimilarity, String topPhoneticMatch) {
        this.proposedTitle = proposedTitle;
        this.normalizedTitle = normalizedTitle;
        this.exactMatch = exactMatch;
        this.matchedTitle = matchedTitle;
        this.candidateTitles = candidateTitles;
        this.decisionStage = decisionStage;
        this.candidateMatches = candidateMatches;
        this.highestFuzzySimilarity = highestFuzzySimilarity;
        this.topFuzzyMatch = topFuzzyMatch;
        this.highestPhoneticSimilarity = highestPhoneticSimilarity;
        this.topPhoneticMatch = topPhoneticMatch;
    }

    public TitleVerificationResultDto(String proposedTitle, String normalizedTitle, boolean exactMatch,
                                      String matchedTitle, List<String> candidateTitles, String decisionStage,
                                      List<CandidateMatchDto> candidateMatches, Double highestFuzzySimilarity,
                                      String topFuzzyMatch, Double highestPhoneticSimilarity, String topPhoneticMatch,
                                      Double highestSemanticSimilarity, String topSemanticMatch) {
        this(proposedTitle, normalizedTitle, exactMatch, matchedTitle, candidateTitles, decisionStage,
             candidateMatches, highestFuzzySimilarity, topFuzzyMatch, highestPhoneticSimilarity, topPhoneticMatch,
             highestSemanticSimilarity, topSemanticMatch, null);
    }

    public TitleVerificationResultDto(String proposedTitle, String normalizedTitle, boolean exactMatch,
                                      String matchedTitle, List<String> candidateTitles, String decisionStage,
                                      List<CandidateMatchDto> candidateMatches, Double highestFuzzySimilarity,
                                      String topFuzzyMatch, Double highestPhoneticSimilarity, String topPhoneticMatch,
                                      Double highestSemanticSimilarity, String topSemanticMatch,
                                      List<RuleResultDto> ruleResults) {
        this.proposedTitle = proposedTitle;
        this.normalizedTitle = normalizedTitle;
        this.exactMatch = exactMatch;
        this.matchedTitle = matchedTitle;
        this.candidateTitles = candidateTitles;
        this.decisionStage = decisionStage;
        this.candidateMatches = candidateMatches;
        this.highestFuzzySimilarity = highestFuzzySimilarity;
        this.topFuzzyMatch = topFuzzyMatch;
        this.highestPhoneticSimilarity = highestPhoneticSimilarity;
        this.topPhoneticMatch = topPhoneticMatch;
        this.highestSemanticSimilarity = highestSemanticSimilarity;
        this.topSemanticMatch = topSemanticMatch;
        this.ruleResults = ruleResults;
    }

    public TitleVerificationResultDto(String proposedTitle, String normalizedTitle, boolean exactMatch,
                                      String matchedTitle, List<String> candidateTitles, String decisionStage,
                                      List<CandidateMatchDto> candidateMatches, Double highestFuzzySimilarity,
                                      String topFuzzyMatch, Double highestPhoneticSimilarity, String topPhoneticMatch,
                                      Double highestSemanticSimilarity, String topSemanticMatch,
                                      List<RuleResultDto> ruleResults, String finalDecision, Double riskScore,
                                      String riskLevel, Double approvalConfidence, String closestMatch,
                                      List<String> reasons, String recommendation) {
        this.proposedTitle = proposedTitle;
        this.normalizedTitle = normalizedTitle;
        this.exactMatch = exactMatch;
        this.matchedTitle = matchedTitle;
        this.candidateTitles = candidateTitles;
        this.decisionStage = decisionStage;
        this.candidateMatches = candidateMatches;
        this.highestFuzzySimilarity = highestFuzzySimilarity;
        this.topFuzzyMatch = topFuzzyMatch;
        this.highestPhoneticSimilarity = highestPhoneticSimilarity;
        this.topPhoneticMatch = topPhoneticMatch;
        this.highestSemanticSimilarity = highestSemanticSimilarity;
        this.topSemanticMatch = topSemanticMatch;
        this.ruleResults = ruleResults;
        this.finalDecision = finalDecision;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.approvalConfidence = approvalConfidence;
        this.closestMatch = closestMatch;
        this.reasons = reasons;
        this.recommendation = recommendation;
    }

    public String getProposedTitle() {
        return proposedTitle;
    }

    public void setProposedTitle(String proposedTitle) {
        this.proposedTitle = proposedTitle;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getMatchedTitle() {
        return matchedTitle;
    }

    public void setMatchedTitle(String matchedTitle) {
        this.matchedTitle = matchedTitle;
    }

    public List<String> getCandidateTitles() {
        return candidateTitles;
    }

    public void setCandidateTitles(List<String> candidateTitles) {
        this.candidateTitles = candidateTitles;
    }

    public String getDecisionStage() {
        return decisionStage;
    }

    public void setDecisionStage(String decisionStage) {
        this.decisionStage = decisionStage;
    }

    public List<CandidateMatchDto> getCandidateMatches() {
        return candidateMatches;
    }

    public void setCandidateMatches(List<CandidateMatchDto> candidateMatches) {
        this.candidateMatches = candidateMatches;
    }

    public Double getHighestFuzzySimilarity() {
        return highestFuzzySimilarity;
    }

    public void setHighestFuzzySimilarity(Double highestFuzzySimilarity) {
        this.highestFuzzySimilarity = highestFuzzySimilarity;
    }

    public String getTopFuzzyMatch() {
        return topFuzzyMatch;
    }

    public void setTopFuzzyMatch(String topFuzzyMatch) {
        this.topFuzzyMatch = topFuzzyMatch;
    }

    public Double getHighestPhoneticSimilarity() {
        return highestPhoneticSimilarity;
    }

    public void setHighestPhoneticSimilarity(Double highestPhoneticSimilarity) {
        this.highestPhoneticSimilarity = highestPhoneticSimilarity;
    }

    public String getTopPhoneticMatch() {
        return topPhoneticMatch;
    }

    public void setTopPhoneticMatch(String topPhoneticMatch) {
        this.topPhoneticMatch = topPhoneticMatch;
    }

    public Double getHighestSemanticSimilarity() {
        return highestSemanticSimilarity;
    }

    public void setHighestSemanticSimilarity(Double highestSemanticSimilarity) {
        this.highestSemanticSimilarity = highestSemanticSimilarity;
    }

    public String getTopSemanticMatch() {
        return topSemanticMatch;
    }

    public void setTopSemanticMatch(String topSemanticMatch) {
        this.topSemanticMatch = topSemanticMatch;
    }

    public List<RuleResultDto> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(List<RuleResultDto> ruleResults) {
        this.ruleResults = ruleResults;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getApprovalConfidence() {
        return approvalConfidence;
    }

    public void setApprovalConfidence(Double approvalConfidence) {
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
