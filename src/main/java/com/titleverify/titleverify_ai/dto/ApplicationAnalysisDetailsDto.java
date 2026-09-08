package com.titleverify.titleverify_ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ApplicationAnalysisDetailsDto {

    private Long applicationId;
    private String publicationType;
    private String language;
    private String state;
    private String district;
    private String periodicity;
    private LocalDateTime createdAt;
    private List<TitleVerificationResultDto> verificationResults;
    private int totalCount;
    private int lowRiskCount;
    private int reviewCount;
    private int highRiskCount;

    public ApplicationAnalysisDetailsDto() {
    }

    public ApplicationAnalysisDetailsDto(Long applicationId, String publicationType, String language,
                                         String state, String district, String periodicity,
                                         LocalDateTime createdAt, List<TitleVerificationResultDto> verificationResults,
                                         int totalCount, int lowRiskCount, int reviewCount, int highRiskCount) {
        this.applicationId = applicationId;
        this.publicationType = publicationType;
        this.language = language;
        this.state = state;
        this.district = district;
        this.periodicity = periodicity;
        this.createdAt = createdAt;
        this.verificationResults = verificationResults;
        this.totalCount = totalCount;
        this.lowRiskCount = lowRiskCount;
        this.reviewCount = reviewCount;
        this.highRiskCount = highRiskCount;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TitleVerificationResultDto> getVerificationResults() {
        return verificationResults;
    }

    public void setVerificationResults(List<TitleVerificationResultDto> verificationResults) {
        this.verificationResults = verificationResults;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getLowRiskCount() {
        return lowRiskCount;
    }

    public void setLowRiskCount(int lowRiskCount) {
        this.lowRiskCount = lowRiskCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(int highRiskCount) {
        this.highRiskCount = highRiskCount;
    }
}
