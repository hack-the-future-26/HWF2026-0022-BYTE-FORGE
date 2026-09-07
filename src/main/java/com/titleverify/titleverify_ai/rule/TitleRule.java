package com.titleverify.titleverify_ai.rule;

import com.titleverify.titleverify_ai.dto.RuleResultDto;

public interface TitleRule {

    RuleResultDto evaluate(RuleEngineInput input);

    String getRuleId();

    String getRuleName();
}
