package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.impl.ProhibitedWordRule;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProhibitedWordRuleTest {

    private ProhibitedWordRule rule;
    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        rule = new ProhibitedWordRule("police,crime,corruption,cbi,cid,army");
        normalizationService = new TitleNormalizationService();
    }

    @Test
    @DisplayName("Should trigger HIGH severity when proposed title contains restricted term (e.g. Crime Today)")
    void testProhibitedWordTriggered() {
        String proposed = "Crime Today";
        String normalized = normalizationService.normalize(proposed);

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", List.of());
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.HIGH, result.getSeverity());
        assertEquals("PROHIBITED_WORD", result.getRuleId());
        assertTrue(result.getEvidence().contains("crime"));
        assertNotNull(result.getRecommendation());
    }

    @Test
    @DisplayName("Should NOT trigger rule for clean titles without restricted terms")
    void testProhibitedWordCleanTitle() {
        String proposed = "Morning Express";
        String normalized = normalizationService.normalize(proposed);

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", List.of());
        RuleResultDto result = rule.evaluate(input);

        assertFalse(result.isTriggered());
        assertEquals(RuleSeverity.INFO, result.getSeverity());
    }
}
