package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.impl.PeriodicityModifierRule;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeriodicityModifierRuleTest {

    private PeriodicityModifierRule rule;
    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        rule = new PeriodicityModifierRule("daily,weekly,fortnightly,monthly,biweekly,bimonthly,quarterly,annual,yearly,evening,morning,express,gazette,bulletin");
        normalizationService = new TitleNormalizationService();
    }

    @Test
    @DisplayName("Should detect periodicity modifier (e.g. India News Daily vs candidate India News)")
    void testPeriodicityModifierDetected() {
        String proposed = "India News Daily";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("India News");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.WARNING, result.getSeverity());
        assertEquals("PERIODICITY_MODIFIER", result.getRuleId());
        assertTrue(result.getEvidence().contains("daily"));
        assertTrue(result.getEvidence().contains("India News"));
    }

    @Test
    @DisplayName("Should NOT trigger periodicity modifier rule when no candidate matches base title")
    void testPeriodicityModifierNotTriggered() {
        String proposed = "National Herald Daily";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("Agriculture Weekly");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertFalse(result.isTriggered());
        assertEquals(RuleSeverity.INFO, result.getSeverity());
    }
}
