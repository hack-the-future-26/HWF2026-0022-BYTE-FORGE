package com.titleverify.titleverify_ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publication_application")
public class PublicationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publication_type", nullable = false)
    private String publicationType;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "periodicity", nullable = false)
    private String periodicity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposedTitle> proposedTitles = new ArrayList<>();

    public PublicationApplication() {
    }

    public PublicationApplication(String publicationType, String language, String state, String district, String periodicity) {
        this.publicationType = publicationType;
        this.language = language;
        this.state = state;
        this.district = district;
        this.periodicity = periodicity;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addProposedTitle(ProposedTitle title) {
        proposedTitles.add(title);
        title.setApplication(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<ProposedTitle> getProposedTitles() {
        return proposedTitles;
    }

    public void setProposedTitles(List<ProposedTitle> proposedTitles) {
        this.proposedTitles = proposedTitles;
    }
}
