package com.titleverify.titleverify_ai.dto;

public class HeadlineVerificationResultDto {

    private String generatedHeadline;
    private double contentRelevance; // 0.0 to 100.0%
    private TitleVerificationResultDto verificationResult;
    private boolean verificationError;
    private String errorMessage;

    public HeadlineVerificationResultDto() {
    }

    public HeadlineVerificationResultDto(String generatedHeadline, double contentRelevance,
            TitleVerificationResultDto verificationResult) {
        this.generatedHeadline = generatedHeadline;
        this.contentRelevance = contentRelevance;
        this.verificationResult = verificationResult;
        this.verificationError = false;
        this.errorMessage = null;
    }

    public HeadlineVerificationResultDto(String generatedHeadline, double contentRelevance, String errorMessage) {
        this.generatedHeadline = generatedHeadline;
        this.contentRelevance = contentRelevance;
        this.verificationResult = null;
        this.verificationError = true;
        this.errorMessage = errorMessage;
    }

    public String getGeneratedHeadline() {
        return generatedHeadline;
    }

    public void setGeneratedHeadline(String generatedHeadline) {
        this.generatedHeadline = generatedHeadline;
    }

    public double getContentRelevance() {
        return contentRelevance;
    }

    public void setContentRelevance(double contentRelevance) {
        this.contentRelevance = contentRelevance;
    }

    public TitleVerificationResultDto getVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(TitleVerificationResultDto verificationResult) {
        this.verificationResult = verificationResult;
    }

    public boolean isVerificationError() {
        return verificationError;
    }

    public void setVerificationError(boolean verificationError) {
        this.verificationError = verificationError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSemanticMatchDisplay() {
        if (verificationError || verificationResult == null) {
            return "Verification unavailable";
        }
        if (verificationResult.getCandidateMatches() == null || verificationResult.getCandidateMatches().isEmpty()) {
            return "No comparable registered title found";
        }
        if (verificationResult.getHighestSemanticSimilarity() == null) {
            return "Semantic analysis unavailable";
        }
        return String.format("%.1f%%", verificationResult.getHighestSemanticSimilarity() * 100.0);
    }
}
