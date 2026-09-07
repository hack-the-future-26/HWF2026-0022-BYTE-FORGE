package com.titleverify.titleverify_ai.rule;

import java.util.ArrayList;
import java.util.List;

public class RuleEngineInput {

    private String proposedTitle;
    private String normalizedTitle;
    private String publicationType;
    private String language;
    private String state;
    private String district;
    private String periodicity;
    private List<String> candidateTitles;

    public RuleEngineInput() {
        this.candidateTitles = new ArrayList<>();
    }

    public RuleEngineInput(String proposedTitle, String normalizedTitle, String publicationType,
                           String language, String state, String district, String periodicity,
                           List<String> candidateTitles) {
        this.proposedTitle = proposedTitle;
        this.normalizedTitle = normalizedTitle;
        this.publicationType = publicationType;
        this.language = language;
        this.state = state;
        this.district = district;
        this.periodicity = periodicity;
        this.candidateTitles = candidateTitles != null ? candidateTitles : new ArrayList<>();
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

    public List<String> getCandidateTitles() {
        return candidateTitles;
    }

    public void setCandidateTitles(List<String> candidateTitles) {
        this.candidateTitles = candidateTitles != null ? candidateTitles : new ArrayList<>();
    }
}
