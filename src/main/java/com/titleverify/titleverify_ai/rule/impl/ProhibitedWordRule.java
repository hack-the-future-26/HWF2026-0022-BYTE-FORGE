package com.titleverify.titleverify_ai.rule.impl;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.TitleRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProhibitedWordRule implements TitleRule {

    private final List<String> restrictedWords;

    public ProhibitedWordRule(
            @Value("${titleverify.rules.prohibited-words:police,crime,corruption,cbi,cid,army}") String prohibitedWordsCsv) {
        this.restrictedWords = Arrays.stream(prohibitedWordsCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public RuleResultDto evaluate(RuleEngineInput input) {
        if (input == null || input.getNormalizedTitle() == null || input.getNormalizedTitle().isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No valid title provided for prohibited word check.", null,
                    "Ensure a non-empty title is submitted.");
        }

        String normalized = input.getNormalizedTitle().toLowerCase();
        List<String> tokens = Arrays.asList(normalized.split("\\s+"));
        List<String> matchedWords = new ArrayList<>();

        for (String word : restrictedWords) {
            if (tokens.contains(word) || normalized.contains(" " + word + " ") || normalized.startsWith(word + " ")
                    || normalized.endsWith(" " + word) || normalized.equals(word)) {
                matchedWords.add(word);
            }
        }

        if (!matchedWords.isEmpty()) {
            String evidenceStr = "Matched restricted word(s): [" + String.join(", ", matchedWords) + "]";
            return new RuleResultDto(
                    getRuleId(),
                    getRuleName(),
                    RuleSeverity.HIGH,
                    true,
                    "Development Rule Warning: Proposed title contains restricted term(s): "
                            + String.join(", ", matchedWords),
                    evidenceStr,
                    "Consider selecting a title that does not contain restricted government, law enforcement, or regulatory agency terms.");
        }

        return new RuleResultDto(
                getRuleId(),
                getRuleName(),
                RuleSeverity.INFO,
                false,
                "No prohibited or restricted words detected in title.",
                "Checked against development restricted word list: " + restrictedWords,
                "Title passes prohibited word inspection.");
    }

    @Override
    public String getRuleId() {
        return "PROHIBITED_WORD";
    }

    @Override
    public String getRuleName() {
        return "Prohibited / Restricted Word Rule";
    }
}
