package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.impl.PrefixSuffixRule;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrefixSuffixRuleTest {

    private PrefixSuffixRule rule;
    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        rule = new PrefixSuffixRule(
                "the,a,an,shree,shri,new,latest,subh,real,super,prime,grand",
                "news,times,post,today,now,herald,standard,live,media,chronicle,press,journal,samachar,khabar,patrikar"
        );
        normalizationService = new TitleNormalizationService();
    }

    @Test
    @DisplayName("Should detect generic prefix modification (e.g. The India Samachar vs candidate India Samachar)")
    void testPrefixDetected() {
        String proposed = "The India Samachar";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("India Samachar");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "Hindi", "Delhi", "Central", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.WARNING, result.getSeverity());
        assertEquals("PREFIX_SUFFIX", result.getRuleId());
        assertTrue(result.getEvidence().contains("the"));
        assertTrue(result.getEvidence().contains("India Samachar"));
    }

    @Test
    @DisplayName("Should detect generic suffix modification (e.g. Mysuru Chronicle Today vs candidate Mysuru Chronicle)")
    void testSuffixDetected() {
        String proposed = "Mysuru Chronicle Today";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("Mysuru Chronicle");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "Kannada", "Karnataka", "Mysuru", "Daily", candidates);
        RuleResultDto result = rule.evaluate(input);

        assertTrue(result.isTriggered());
        assertEquals(RuleSeverity.WARNING, result.getSeverity());
        assertEquals("PREFIX_SUFFIX", result.getRuleId());
        assertTrue(result.getEvidence().contains("today"));
    }
}
