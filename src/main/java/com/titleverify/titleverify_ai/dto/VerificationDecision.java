package com.titleverify.titleverify_ai.dto;

public enum VerificationDecision {
    ACCEPT("ACCEPT", "Low Verification Risk"),
    REVIEW("REVIEW", "Borderline Risk / Requires Manual Review"),
    HIGH_RISK("HIGH RISK", "High Verification Risk");

    private final String code;
    private final String displayName;

    VerificationDecision(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
