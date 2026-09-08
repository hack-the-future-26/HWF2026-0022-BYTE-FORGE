package com.titleverify.titleverify_ai.rule.impl;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import com.titleverify.titleverify_ai.rule.TitleRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContextRule implements TitleRule {

    @Override
    public RuleResultDto evaluate(RuleEngineInput input) {
        if (input == null) {
            return new RuleResultDto(getRuleId(), getRuleName(), RuleSeverity.INFO, false,
                    "No application context provided.", null, "Provide application context for full verification.");
        }

        List<String> missingFields = new ArrayList<>();
        if (isEmpty(input.getPublicationType()))
            missingFields.add("publicationType");
        if (isEmpty(input.getLanguage()))
            missingFields.add("language");
        if (isEmpty(input.getState()))
            missingFields.add("state");
        if (isEmpty(input.getDistrict()))
            missingFields.add("district");
        if (isEmpty(input.getPeriodicity()))
            missingFields.add("periodicity");

        if (!missingFields.isEmpty()) {
            return new RuleResultDto(
                    getRuleId(),
                    getRuleName(),
                    RuleSeverity.WARNING,
                    true,
                    "Incomplete application context. Missing field(s): " + String.join(", ", missingFields),
                    "Missing fields: " + missingFields,
                    "Ensure all application parameters (type, language, state, district, periodicity) are specified.");
        }

        String evidence = String.format("Type: '%s', Language: '%s', State: '%s', District: '%s', Periodicity: '%s'",
                input.getPublicationType(), input.getLanguage(), input.getState(), input.getDistrict(),
                input.getPeriodicity());

        // Context parameters are fully present and valid
        return new RuleResultDto(
                getRuleId(),
                getRuleName(),
                RuleSeverity.INFO,
                false,
                "Application context parameters validated successfully.",
                evidence,
                "Context information is complete. Ready for regional extension rules.");
    }

    @Override
    public String getRuleId() {
        return "CONTEXT_VALIDATION";
    }

    @Override
    public String getRuleName() {
        return "Application Context Rule";
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
