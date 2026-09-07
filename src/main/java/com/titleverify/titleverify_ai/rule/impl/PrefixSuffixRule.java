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
 * Rule: Prefix / Suffix Detection.
 * Detects non-distinctive additions (e.g. 'The', 'Shree', 'New', generic suffixes) around existing candidate titles.
 */
@Component
public class PrefixSuffixRule implements TitleRule {

    private final List<String> prefixes;
    private final List<String> suffixes;

    public PrefixSuffixRule(@Value("${titleverify.rules.prefixes:the,a,an,shree,shri,new,latest,subh,real,super,prime,grand}") String prefixesCsv,
                            @Value("${titleverify.rules.suffixes:news,times,post,today,now,herald,standard,live,media,chronicle,press,journal,samachar,khabar,patrikar}") String suffixesCsv) {
        this.prefixes = Arrays.stream(prefixesCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        this.suffixes = Arrays.stream(suffixesCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public RuleResultDto evaluate(RuleEngineInput input) {
        if (input == null || input.getNormalizedTitle() == null || input.getNormalizedTitle().isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No title provided for prefix/suffix check.", null, null);
        }

        List<String> candidates = input.getCandidateTitles();
        if (candidates == null || candidates.isEmpty()) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No candidate titles available to inspect prefix/suffix modifications.", null, "No action required.");
        }

        String proposedNorm = input.getNormalizedTitle().toLowerCase(Locale.ROOT);

        for (String candidate : candidates) {
            String candNorm = candidate.trim().toLowerCase(Locale.ROOT);
            if (proposedNorm.equals(candNorm)) {
                continue;
            }

            if (proposedNorm.contains(candNorm)) {
                // Check Prefix
                for (String prefix : prefixes) {
                    if (proposedNorm.startsWith(prefix + " ")) {
                        String evidence = String.format("Matched Candidate: '%s', Detected Prefix: '%s'", candidate, prefix);
                        return new RuleResultDto(
                                getRuleId(),
                                getRuleName(),
                                RuleSeverity.WARNING,
                                true,
                                "Generic prefix modification detected against candidate title '" + candidate + "'.",
                                evidence,
                                "Adding generic or non-distinctive prefixes (such as '" + prefix + "') to an existing registered title may lead to title rejection."
                        );
                    }
                }

                // Check Suffix
                for (String suffix : suffixes) {
                    if (proposedNorm.endsWith(" " + suffix)) {
                        String evidence = String.format("Matched Candidate: '%s', Detected Suffix: '%s'", candidate, suffix);
                        return new RuleResultDto(
                                getRuleId(),
                                getRuleName(),
                                RuleSeverity.WARNING,
                                true,
                                "Generic suffix modification detected against candidate title '" + candidate + "'.",
                                evidence,
                                "Adding generic suffixes (such as '" + suffix + "') to an existing registered title may cause similarity conflicts."
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
                "No non-distinctive prefix or suffix modifications detected against candidate titles.",
                null,
                "Title passes prefix/suffix inspection."
        );
    }

    @Override
    public String getRuleId() {
        return "PREFIX_SUFFIX";
    }

    @Override
    public String getRuleName() {
        return "Prefix / Suffix Rule";
    }
}
