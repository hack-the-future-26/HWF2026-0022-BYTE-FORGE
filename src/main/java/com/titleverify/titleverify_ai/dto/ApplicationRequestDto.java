package com.titleverify.titleverify_ai.dto;

import java.util.List;

public class ApplicationRequestDto {

    private String publicationType;
    private String language;
    private String state;
    private String district;
    private String periodicity;
    private List<String> proposedTitles;

    public ApplicationRequestDto() {
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

    public List<String> getProposedTitles() {
        return proposedTitles;
    }

    public void setProposedTitles(List<String> proposedTitles) {
        this.proposedTitles = proposedTitles;
    }
}
