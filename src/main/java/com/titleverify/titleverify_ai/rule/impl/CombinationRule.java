package com.titleverify.titleverify_ai.rule.impl;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.TitleRule;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rule: Title Combination Detection.
 * Detects when a proposed title appears to combine meaningful parts of multiple existing candidate titles.
 */
@Component
public class CombinationRule implements TitleRule {

    @Override
    public RuleResultDto evaluate(RuleEngineInput input) {
        if (input == null || input.getNormalizedTitle() == null || input.getNormalizedTitle().isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No title provided for combination rule check.", null, null);
        }

        List<String> candidates = input.getCandidateTitles();
        if (candidates == null || candidates.size() < 2) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "Fewer than 2 candidate titles available for combination detection.", null, "No action required.");
        }

        String proposedNorm = input.getNormalizedTitle().toLowerCase(Locale.ROOT);
        Set<String> matchedCandidates = new LinkedHashSet<>();

        for (int i = 0; i < candidates.size(); i++) {
            String cand1Norm = candidates.get(i).trim().toLowerCase(Locale.ROOT);
            if (cand1Norm.isEmpty()) continue;

            for (int j = i + 1; j < candidates.size(); j++) {
                String cand2Norm = candidates.get(j).trim().toLowerCase(Locale.ROOT);
                if (cand2Norm.isEmpty()) continue;

                // Case 1: Proposed title equals Candidate 1 + Candidate 2 or Candidate 2 + Candidate 1
                if (proposedNorm.equals(cand1Norm + " " + cand2Norm) || proposedNorm.equals(cand2Norm + " " + cand1Norm)) {
                    matchedCandidates.add(candidates.get(i));
                    matchedCandidates.add(candidates.get(j));
                }
                // Case 2: Proposed title contains both candidates as distinct phrases
                else if (proposedNorm.contains(cand1Norm) && proposedNorm.contains(cand2Norm) && !cand1Norm.contains(cand2Norm) && !cand2Norm.contains(cand1Norm)) {
                    matchedCandidates.add(candidates.get(i));
                    matchedCandidates.add(candidates.get(j));
                }
            }
        }

        if (matchedCandidates.size() >= 2) {
            String candidateListStr = matchedCandidates.stream().collect(Collectors.joining(", "));
            String evidenceStr = "Proposed title combines terms from registered candidates: [" + candidateListStr + "]";

            return new RuleResultDto(
                    getRuleId(),
                    getRuleName(),
                    RuleSeverity.WARNING,
                    true,
                    "Proposed title appears to combine parts of multiple existing registered titles.",
                    evidenceStr,
                    "Combining key terms from multiple registered titles may cause public confusion or similarity objections."
            );
        }

        return new RuleResultDto(
                getRuleId(),
                getRuleName(),
                RuleSeverity.INFO,
                false,
                "No candidate title combination detected.",
                null,
                "Title passes combination inspection."
        );
    }

    @Override
    public String getRuleId() {
        return "COMBINATION";
    }

    @Override
    public String getRuleName() {
        return "Combination Rule";
    }
}
