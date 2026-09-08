package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.impl.ContextRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextRuleTest {

    private ContextRule rule;

    @BeforeEach
    void setUp() {
        rule = new ContextRule();
    }

    @Test
    @DisplayName("Should validate complete application context without errors")
    void testCompleteContextValidation() {
        RuleEngineInput input = new RuleEngineInput(
                "Daily Mirror", "daily mirror", "Newspaper", "English", "Delhi", "Central", "Daily", List.of()
        );
        RuleResultDto result = rule.evaluate(input);

        assertFalse(result.isTriggered());
        assertEquals(RuleSeverity.INFO, result.getSeverity());
        assertEquals("CONTEXT_VALIDATION", result.getRuleId());
        assertTrue(result.getEvidence().contains("Newspaper"));
    }

    @Test
    @DisplayName("Should trigger WARNING severity when mandatory context parameters are missing")
    void testIncompleteContextValidation() {
        RuleEngineInput input = new RuleEngineInput(
                "Daily Mirror", "daily mirror", "", "English", "Delhi", "", "Daily", List.of()
        );
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.WARNING, result.getSeverity());
        assertTrue(result.getMessage().contains("publicationType"));
        assertTrue(result.getMessage().contains("district"));
    }
}
