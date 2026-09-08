package com.titleverify.titleverify_ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ApplicationResponseDto {

    private Long id;
    private String status;
    private String message;
    private LocalDateTime createdAt;
    private List<TitleVerificationResultDto> verificationResults;

    public ApplicationResponseDto() {
    }

    public ApplicationResponseDto(Long id, String status, String message, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public ApplicationResponseDto(Long id, String status, String message, LocalDateTime createdAt, List<TitleVerificationResultDto> verificationResults) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.verificationResults = verificationResults;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
