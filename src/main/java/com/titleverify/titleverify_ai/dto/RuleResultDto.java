package com.titleverify.titleverify_ai.dto;

public class RuleResultDto {

    private String ruleId;
    private String ruleName;
    private RuleSeverity severity;
    private boolean triggered;
    private String message;
    private String evidence;
    private String recommendation;

    public RuleResultDto() {
    }

    public RuleResultDto(String ruleId, String ruleName, RuleSeverity severity, boolean triggered,
                         String message, String evidence, String recommendation) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.triggered = triggered;
        this.message = message;
        this.evidence = evidence;
        this.recommendation = recommendation;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
