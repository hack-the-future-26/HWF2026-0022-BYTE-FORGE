package com.titleverify.titleverify_ai.dto;

import java.util.List;

public class HeadlineGenerationResponseDto {

    private List<HeadlineVerificationResultDto> results;
    private HeadlineVerificationResultDto recommendedHeadline;
    private String recommendationReason;
    private String status;
    private String message;

    public HeadlineGenerationResponseDto() {
    }

    public HeadlineGenerationResponseDto(List<HeadlineVerificationResultDto> results,
                                        HeadlineVerificationResultDto recommendedHeadline,
                                        String recommendationReason,
                                        String status,
                                        String message) {
        this.results = results;
        this.recommendedHeadline = recommendedHeadline;
        this.recommendationReason = recommendationReason;
        this.status = status;
        this.message = message;
    }

    public List<HeadlineVerificationResultDto> getResults() {
        return results;
    }

    public void setResults(List<HeadlineVerificationResultDto> results) {
        this.results = results;
    }

    public HeadlineVerificationResultDto getRecommendedHeadline() {
        return recommendedHeadline;
    }

    public void setRecommendedHeadline(HeadlineVerificationResultDto recommendedHeadline) {
        this.recommendedHeadline = recommendedHeadline;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
