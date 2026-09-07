package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.TitleRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleEngineService {

    private final List<TitleRule> rules;

    @Autowired
    public RuleEngineService(List<TitleRule> rules) {
        this.rules = rules != null ? rules : List.of();
    }

    /**
     * Evaluates all registered title rules deterministically against the provided
     * input context.
     *
     * @param input RuleEngineInput containing title and application context
     * @return List of RuleResultDto containing findings from each rule
     */
    public List<RuleResultDto> evaluateRules(RuleEngineInput input) {
        List<RuleResultDto> results = new ArrayList<>();
        if (input == null) {
            return results;
        }

        for (TitleRule rule : rules) {
            try {
                RuleResultDto result = rule.evaluate(input);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                // Defensive exception handling per rule
                results.add(new RuleResultDto(
                        rule.getRuleId(),
                        rule.getRuleName(),
                        com.titleverify.titleverify_ai.dto.RuleSeverity.INFO,
                        false,
                        "Rule execution error: " + e.getMessage(),
                        null,
                        "Rule skipped due to execution error."));
            }
        }

        return results;
    }

    public List<TitleRule> getRules() {
        return rules;
    }
}
