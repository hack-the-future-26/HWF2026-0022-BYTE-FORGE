package com.titleverify.titleverify_ai.rule.impl;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.TitleRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Rule: Periodicity Modifier Detection.
 * Detects when a proposed title contains an existing candidate title along with an added periodicity-style modifier.
 */
@Component
public class PeriodicityModifierRule implements TitleRule {

    private final List<String> periodicityModifiers;

    public PeriodicityModifierRule(@Value("${titleverify.rules.periodicity-modifiers:daily,weekly,fortnightly,monthly,biweekly,bimonthly,quarterly,annual,yearly,evening,morning,express,gazette,bulletin}") String modifiersCsv) {
        this.periodicityModifiers = Arrays.stream(modifiersCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public RuleResultDto evaluate(RuleEngineInput input) {
        if (input == null || input.getNormalizedTitle() == null || input.getNormalizedTitle().isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No title provided for periodicity modifier check.", null, null);
        }

        List<String> candidates = input.getCandidateTitles();
        if (candidates == null || candidates.isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No candidate titles to compare for periodicity modifier detection.", null, "No action required.");
        }

        String proposedNorm = input.getNormalizedTitle().toLowerCase(Locale.ROOT);

        for (String candidate : candidates) {
            String candNorm = candidate.trim().toLowerCase(Locale.ROOT);
            if (proposedNorm.equals(candNorm)) {
                continue;
            }

            if (proposedNorm.contains(candNorm)) {
                for (String modifier : periodicityModifiers) {
                    if (proposedNorm.endsWith(" " + modifier) ||
                        proposedNorm.startsWith(modifier + " ") ||
                        proposedNorm.contains(" " + modifier + " ")) {

                        String evidence = String.format("Proposed title '%s' contains candidate '%s' with periodicity modifier '%s'",
                                input.getProposedTitle(), candidate, modifier);

                        return new RuleResultDto(
                                getRuleId(),
                                getRuleName(),
                                RuleSeverity.WARNING,
                                true,
                                "Periodicity modifier addition detected against existing title '" + candidate + "'.",
                                evidence,
                                "Adding a periodicity modifier (e.g., Daily, Weekly) to an existing registered title may not create sufficient distinctiveness."
                        );
                    }
                }
            }
        }

        return new RuleResultDto(
                getRuleId(),
                getRuleName(),
                RuleSeverity.INFO,
                false,
                "No periodicity modifier additions detected against candidate titles.",
                null,
                "Title passes periodicity modifier check."
        );
    }

    @Override
    public String getRuleId() {
        return "PERIODICITY_MODIFIER";
    }

    @Override
    public String getRuleName() {
        return "Periodicity Modifier Rule";
    }
}
