package com.titleverify.titleverify_ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registered_publication_title", indexes = {
    @Index(name = "idx_reg_title_norm", columnList = "normalized_title")
})
public class RegisteredPublicationTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    @Column(name = "language")
    private String language;

    @Column(name = "state")
    private String state;

    @Column(name = "periodicity")
    private String periodicity;

    @Column(name = "publication_type")
    private String publicationType;

    @Column(name = "status")
    private String status;

    public RegisteredPublicationTitle() {
    }

    public RegisteredPublicationTitle(String title, String normalizedTitle, String language, String state, String periodicity, String publicationType, String status) {
        this.title = title;
        this.normalizedTitle = normalizedTitle;
        this.language = language;
        this.state = state;
        this.periodicity = periodicity;
        this.publicationType = publicationType;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
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

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
