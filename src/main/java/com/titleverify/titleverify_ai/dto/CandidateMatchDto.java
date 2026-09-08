package com.titleverify.titleverify_ai.dto;

public class CandidateMatchDto {

    private String candidateTitle;
    private String normalizedCandidateTitle;
    private double similarityScore;
    private String similarityLevel;
    private double phoneticSimilarityScore;
    private String phoneticSimilarityLevel;
    private Double semanticSimilarityScore;
    private String semanticSimilarityLevel;

    public CandidateMatchDto() {
    }

    public CandidateMatchDto(String candidateTitle, String normalizedCandidateTitle, double similarityScore, String similarityLevel) {
        this.candidateTitle = candidateTitle;
        this.normalizedCandidateTitle = normalizedCandidateTitle;
        this.similarityScore = similarityScore;
        this.similarityLevel = similarityLevel;
    }

    public CandidateMatchDto(String candidateTitle, String normalizedCandidateTitle, double similarityScore, String similarityLevel,
                             double phoneticSimilarityScore, String phoneticSimilarityLevel) {
        this.candidateTitle = candidateTitle;
        this.normalizedCandidateTitle = normalizedCandidateTitle;
        this.similarityScore = similarityScore;
        this.similarityLevel = similarityLevel;
        this.phoneticSimilarityScore = phoneticSimilarityScore;
        this.phoneticSimilarityLevel = phoneticSimilarityLevel;
    }

    public CandidateMatchDto(String candidateTitle, String normalizedCandidateTitle, double similarityScore, String similarityLevel,
                             double phoneticSimilarityScore, String phoneticSimilarityLevel,
                             Double semanticSimilarityScore, String semanticSimilarityLevel) {
        this.candidateTitle = candidateTitle;
        this.normalizedCandidateTitle = normalizedCandidateTitle;
        this.similarityScore = similarityScore;
        this.similarityLevel = similarityLevel;
        this.phoneticSimilarityScore = phoneticSimilarityScore;
        this.phoneticSimilarityLevel = phoneticSimilarityLevel;
        this.semanticSimilarityScore = semanticSimilarityScore;
        this.semanticSimilarityLevel = semanticSimilarityLevel;
    }

    public String getCandidateTitle() {
        return candidateTitle;
    }

    public void setCandidateTitle(String candidateTitle) {
        this.candidateTitle = candidateTitle;
    }

    public String getNormalizedCandidateTitle() {
        return normalizedCandidateTitle;
    }

    public void setNormalizedCandidateTitle(String normalizedCandidateTitle) {
        this.normalizedCandidateTitle = normalizedCandidateTitle;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getSimilarityLevel() {
        return similarityLevel;
    }

    public void setSimilarityLevel(String similarityLevel) {
        this.similarityLevel = similarityLevel;
    }

    public double getPhoneticSimilarityScore() {
        return phoneticSimilarityScore;
    }

    public void setPhoneticSimilarityScore(double phoneticSimilarityScore) {
        this.phoneticSimilarityScore = phoneticSimilarityScore;
    }

    public String getPhoneticSimilarityLevel() {
        return phoneticSimilarityLevel;
    }

    public void setPhoneticSimilarityLevel(String phoneticSimilarityLevel) {
        this.phoneticSimilarityLevel = phoneticSimilarityLevel;
    }

    public Double getSemanticSimilarityScore() {
        return semanticSimilarityScore;
    }

    public void setSemanticSimilarityScore(Double semanticSimilarityScore) {
        this.semanticSimilarityScore = semanticSimilarityScore;
    }

    public String getSemanticSimilarityLevel() {
        return semanticSimilarityLevel;
    }

    public void setSemanticSimilarityLevel(String semanticSimilarityLevel) {
        this.semanticSimilarityLevel = semanticSimilarityLevel;
    }
}
