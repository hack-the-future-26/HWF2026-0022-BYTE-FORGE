package com.titleverify.titleverify_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HeadlineGenerationRequestDto {

    @NotBlank(message = "News topic or article content is required")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;

    private String publicationType;
    private String language;
    private String state;
    private String district;
    private String periodicity;

    public HeadlineGenerationRequestDto() {
    }

    public HeadlineGenerationRequestDto(String content) {
        this(content, "Newspaper", "English", "Karnataka", "Bengaluru Urban", "Daily");
    }

    public HeadlineGenerationRequestDto(String content, String publicationType, String language,
                                        String state, String district, String periodicity) {
        this.content = content;
        this.publicationType = publicationType;
        this.language = language;
        this.state = state;
        this.district = district;
        this.periodicity = periodicity;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublicationType() {
        return (publicationType != null && !publicationType.isBlank()) ? publicationType : "Newspaper";
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getLanguage() {
        return (language != null && !language.isBlank()) ? language : "English";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getState() {
        return (state != null && !state.isBlank()) ? state : "Karnataka";
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return (district != null && !district.isBlank()) ? district : "Bengaluru Urban";
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPeriodicity() {
        return (periodicity != null && !periodicity.isBlank()) ? periodicity : "Daily";
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }
}
