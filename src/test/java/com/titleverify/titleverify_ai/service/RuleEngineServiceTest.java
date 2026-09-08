package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineServiceTest {

    private RuleEngineService ruleEngineService;
    private TitleNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        ProhibitedWordRule prohibitedRule = new ProhibitedWordRule("police,crime,corruption,cbi,cid,army");
        PeriodicityModifierRule periodicityRule = new PeriodicityModifierRule("daily,weekly,fortnightly,monthly,biweekly,bimonthly,quarterly,annual,yearly,evening,morning,express,gazette,bulletin");
        PrefixSuffixRule prefixSuffixRule = new PrefixSuffixRule("the,a,an,shree,shri,new,latest,subh,real,super,prime,grand", "news,times,post,today,now,herald,standard,live,media,chronicle,press,journal,samachar,khabar,patrikar");
        CombinationRule combinationRule = new CombinationRule();
        ContextRule contextRule = new ContextRule();

        ruleEngineService = new RuleEngineService(List.of(
                prohibitedRule, periodicityRule, prefixSuffixRule, combinationRule, contextRule
        ));
    }

    @Test
    @DisplayName("Clean title with no rule violations should produce no HIGH severity findings")
    void testCleanTitleNoHighSeverityFindings() {
        String proposed = "Morning Express";
        String normalized = normalizationService.normalize(proposed);
        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", List.of("Agriculture Weekly"));

        List<RuleResultDto> results = ruleEngineService.evaluateRules(input);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        boolean hasHighSeverity = results.stream().anyMatch(r -> r.getSeverity() == RuleSeverity.HIGH && r.isTriggered());
        assertFalse(hasHighSeverity, "Expected clean title to have no HIGH severity triggered findings");
    }

    @Test
    @DisplayName("Title triggering multiple rules (Crime India News Daily) should return all relevant rule results")
    void testMultipleRuleViolationsReturned() {
        String proposed = "Crime India News Daily";
        String normalized = normalizationService.normalize(proposed);
        List<String> candidates = List.of("India News");

        RuleEngineInput input = new RuleEngineInput(proposed, normalized, "Newspaper", "English", "Delhi", "Central", "Daily", candidates);

        List<RuleResultDto> results = ruleEngineService.evaluateRules(input);

        assertNotNull(results);
        long triggeredCount = results.stream().filter(RuleResultDto::isTriggered).count();
        assertTrue(triggeredCount >= 2, "Expected at least 2 triggered rules for multi-violation title");

        boolean prohibitedTriggered = results.stream().anyMatch(r -> "PROHIBITED_WORD".equals(r.getRuleId()) && r.isTriggered() && r.getSeverity() == RuleSeverity.HIGH);
        boolean periodicityTriggered = results.stream().anyMatch(r -> "PERIODICITY_MODIFIER".equals(r.getRuleId()) && r.isTriggered());

        assertTrue(prohibitedTriggered, "Prohibited word rule should be triggered with HIGH severity");
        assertTrue(periodicityTriggered, "Periodicity modifier rule should be triggered");
    }
}
