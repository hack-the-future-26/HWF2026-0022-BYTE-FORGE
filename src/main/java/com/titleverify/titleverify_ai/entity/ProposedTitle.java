package com.titleverify.titleverify_ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "proposed_title")
public class ProposedTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_order", nullable = false)
    private Integer titleOrder;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    @Column(name = "exact_match")
    private Boolean exactMatch;

    @Column(name = "matched_title")
    private String matchedTitle;

    @Column(name = "verification_stage")
    private String verificationStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonIgnore
    private PublicationApplication application;

    public ProposedTitle() {
    }

    public ProposedTitle(String title, Integer titleOrder) {
        this.title = title;
        this.titleOrder = titleOrder;
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

    public Integer getTitleOrder() {
        return titleOrder;
    }

    public void setTitleOrder(Integer titleOrder) {
        this.titleOrder = titleOrder;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }

    public Boolean getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getMatchedTitle() {
        return matchedTitle;
    }

    public void setMatchedTitle(String matchedTitle) {
        this.matchedTitle = matchedTitle;
    }

    public String getVerificationStage() {
        return verificationStage;
    }

    public void setVerificationStage(String verificationStage) {
        this.verificationStage = verificationStage;
    }

    public PublicationApplication getApplication() {
        return application;
    }

    public void setApplication(PublicationApplication application) {
        this.application = application;
    }
}
