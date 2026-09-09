package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.impl.CombinationRule;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombinationRuleTest {

    private CombinationRule rule;
    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        rule = new CombinationRule();
        normalizationService = new TitleNormalizationService();
    }

    @Test
    @DisplayName("Should detect title combining multiple candidate titles (e.g. Hindu Indian Express)")
    void testCombinationDetected() {
        String proposed = "Hindu Indian Express";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("Hindu", "Indian Express");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.WARNING, result.getSeverity());
        assertEquals("COMBINATION", result.getRuleId());
        assertTrue(result.getEvidence().contains("Hindu"));
        assertTrue(result.getEvidence().contains("Indian Express"));
    }

    @Test
    @DisplayName("Should NOT trigger combination rule when proposed title does not combine multiple candidates")
    void testCombinationNotTriggered() {
        String proposed = "Morning Chronicle";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("Hindu", "Indian Express");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertFalse(result.isTriggered());
        assertEquals(RuleSeverity.INFO, result.getSeverity());
    }
}
