package com.titleverify.titleverify_ai.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VerificationHistoryItemDto {

    private Long applicationId;
    private LocalDateTime createdAt;
    private String publicationType;
    private String language;
    private String state;
    private String district;
    private String periodicity;
    private int titleCount;
    private List<String> proposedTitles = new ArrayList<>();
    private String overallDecision;

    public VerificationHistoryItemDto() {
    }

    public VerificationHistoryItemDto(Long applicationId, LocalDateTime createdAt, String publicationType,
                                     String language, String state, String district, String periodicity,
                                     int titleCount, List<String> proposedTitles, String overallDecision) {
        this.applicationId = applicationId;
        this.createdAt = createdAt;
        this.publicationType = publicationType;
        this.language = language;
        this.state = state;
        this.district = district;
        this.periodicity = periodicity;
        this.titleCount = titleCount;
        this.proposedTitles = proposedTitles != null ? proposedTitles : new ArrayList<>();
        this.overallDecision = overallDecision;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "N/A";
        }
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
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

    public int getTitleCount() {
        return titleCount;
    }

    public void setTitleCount(int titleCount) {
        this.titleCount = titleCount;
    }

    public List<String> getProposedTitles() {
        return proposedTitles;
    }

    public void setProposedTitles(List<String> proposedTitles) {
        this.proposedTitles = proposedTitles;
    }

    public String getOverallDecision() {
        return overallDecision;
    }

    public void setOverallDecision(String overallDecision) {
        this.overallDecision = overallDecision;
    }
}
