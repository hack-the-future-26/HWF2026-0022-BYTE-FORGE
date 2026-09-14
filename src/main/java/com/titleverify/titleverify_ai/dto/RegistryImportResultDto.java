package com.titleverify.titleverify_ai.dto;

import java.util.ArrayList;
import java.util.List;

public class RegistryImportResultDto {

    private int totalProcessed;
    private int insertedCount;
    private int duplicateCount;
    private int invalidCount;
    private int errorCount;
    private List<String> errorDetails = new ArrayList<>();

    public RegistryImportResultDto() {
    }

    public RegistryImportResultDto(int totalProcessed, int insertedCount, int duplicateCount,
                                   int invalidCount, int errorCount, List<String> errorDetails) {
        this.totalProcessed = totalProcessed;
        this.insertedCount = insertedCount;
        this.duplicateCount = duplicateCount;
        this.invalidCount = invalidCount;
        this.errorCount = errorCount;
        this.errorDetails = errorDetails != null ? new ArrayList<>(errorDetails) : new ArrayList<>();
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails != null ? new ArrayList<>(errorDetails) : new ArrayList<>();
    }

    public void addErrorDetail(String detail) {
        if (detail != null && !detail.isBlank()) {
            this.errorDetails.add(detail);
        }
    }

    public void incrementTotalProcessed() {
        this.totalProcessed++;
    }

    public void incrementInsertedCount() {
        this.insertedCount++;
    }

    public void incrementDuplicateCount() {
        this.duplicateCount++;
    }

    public void incrementInvalidCount() {
        this.invalidCount++;
    }

    public void incrementErrorCount() {
        this.errorCount++;
    }
}
