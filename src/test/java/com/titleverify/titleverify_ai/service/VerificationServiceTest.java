package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.rule.impl.*;
import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class VerificationServiceTest {

    private TitleNormalizationService normalizationService;
    private ExactMatchService exactMatchService;
    private CandidateRetrievalService candidateRetrievalService;
    private FuzzySimilarityService fuzzySimilarityService;
    private PhoneticSimilarityService phoneticSimilarityService;
    private SemanticSimilarityService semanticSimilarityService;
    private RuleEngineService ruleEngineService;
    private RiskScoringService riskScoringService;
    private DecisionEngineService decisionEngineService;
    private DecisionExplanationService decisionExplanationService;
    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        exactMatchService = Mockito.mock(ExactMatchService.class);
        candidateRetrievalService = Mockito.mock(CandidateRetrievalService.class);
        fuzzySimilarityService = new FuzzySimilarityService();
        phoneticSimilarityService = new PhoneticSimilarityService(normalizationService, fuzzySimilarityService);
        EmbeddingService embeddingService = new DevelopmentEmbeddingService(normalizationService);
        CosineSimilarityCalculator cosineSimilarityCalculator = new CosineSimilarityCalculator();
        semanticSimilarityService = new SemanticSimilarityService(embeddingService, cosineSimilarityCalculator);
        ruleEngineService = new RuleEngineService(List.of(
                new ProhibitedWordRule("police,crime,corruption,cbi,cid,army"),
                new PeriodicityModifierRule("daily,weekly,fortnightly,monthly,biweekly,bimonthly,quarterly,annual,yearly,evening,morning,express,gazette,bulletin"),
                new PrefixSuffixRule("the,a,an,shree,shri,new,latest,subh,real,super,prime,grand", "news,times,post,today,now,herald,standard,live,media,chronicle,press,journal,samachar,khabar,patrikar"),
                new CombinationRule(),
                new ContextRule()
        ));
        riskScoringService = new RiskScoringService(100.0, 30.0, 25.0, 25.0, 40.0, 15.0, 0.0);
        decisionEngineService = new DecisionEngineService(40.0, 70.0);
        decisionExplanationService = new DecisionExplanationService();

        verificationService = new VerificationService(
                normalizationService, exactMatchService, candidateRetrievalService,
                fuzzySimilarityService, phoneticSimilarityService, semanticSimilarityService,
                ruleEngineService, riskScoringService, decisionEngineService, decisionExplanationService
        );
    }

    @Test
    @DisplayName("Should detect EXACT_MATCH when proposed title matches registered record after normalization and assign HIGH RISK decision")
    void testVerifyProposedTitle_ExactMatchFound() {
        String proposed = " INDIA   NEWS!!";
        String normalized = "india news";

        RegisteredPublicationTitle match = new RegisteredPublicationTitle(
                "India News", normalized, "English", "Delhi", "Daily", "Newspaper", "DEMO_DATA"
        );

        when(exactMatchService.findExactMatch(normalized)).thenReturn(Optional.of(match));
        when(candidateRetrievalService.retrieveCandidates(normalized)).thenReturn(List.of(match));

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposed);

        assertTrue(result.isExactMatch());
        assertEquals("India News", result.getMatchedTitle());
        assertEquals("EXACT_MATCH_FOUND", result.getDecisionStage());
        assertEquals("india news", result.getNormalizedTitle());
        assertEquals(1.0, result.getHighestFuzzySimilarity(), 0.001);
        assertEquals(1.0, result.getHighestPhoneticSimilarity(), 0.001);
        assertNotNull(result.getHighestSemanticSimilarity());
        assertEquals(1.0, result.getHighestSemanticSimilarity(), 0.001);
        assertNotNull(result.getRuleResults());

        // Decision Engine assertions
        assertEquals("HIGH RISK", result.getFinalDecision());
        assertEquals(100.0, result.getRiskScore());
        assertEquals("HIGH RISK", result.getRiskLevel());
        assertEquals(0.0, result.getApprovalConfidence());
        assertNotNull(result.getReasons());
        assertNotNull(result.getRecommendation());
    }

    @Test
    @DisplayName("Should evaluate fuzzy, phonetic, semantic, rules, risk score, and decision for candidate titles when no exact match")
    void testVerifyProposedTitle_MultiSignalSimilarityCandidates() {
        String proposed = "Evening Daily Bulletin";
        String normalizedProposed = "evening daily bulletin";

        RegisteredPublicationTitle candidate = new RegisteredPublicationTitle(
                "Daily Evening News", "daily evening news", "English", "Delhi", "Daily", "Newspaper", "DEMO_DATA"
        );

        when(exactMatchService.findExactMatch(normalizedProposed)).thenReturn(Optional.empty());
        when(candidateRetrievalService.retrieveCandidates(normalizedProposed)).thenReturn(List.of(candidate));

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposed);

        assertFalse(result.isExactMatch());
        assertEquals("CANDIDATE_SIMILARITY_ANALYSIS", result.getDecisionStage());
        assertNotNull(result.getCandidateMatches());
        assertEquals(1, result.getCandidateMatches().size());
        assertNotNull(result.getHighestSemanticSimilarity());
        assertTrue(result.getHighestSemanticSimilarity() >= 0.85);

        // Decision Engine assertions
        assertNotNull(result.getFinalDecision());
        assertNotNull(result.getRiskScore());
        assertTrue(result.getRiskScore() >= 40.0, "High similarity candidate should result in score >= 40.0");
        assertNotNull(result.getRiskLevel());
        assertNotNull(result.getApprovalConfidence());
        assertNotNull(result.getReasons());
        assertNotNull(result.getRecommendation());
    }
}
