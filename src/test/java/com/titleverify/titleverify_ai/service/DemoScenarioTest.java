package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.config.RegistryDataSeeder;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import com.titleverify.titleverify_ai.rule.impl.*;
import com.titleverify.titleverify_ai.utility.CosineSimilarityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DemoScenarioTest {

    private RegisteredPublicationTitleRepository repository;
    private VerificationService verificationService;
    private List<RegisteredPublicationTitle> demoStore;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RegisteredPublicationTitleRepository.class);
        demoStore = new ArrayList<>();

        // In-memory mock for repository findByNormalizedTitle and findByNormalizedTitleContaining
        when(repository.findByNormalizedTitle(anyString())).thenAnswer(invocation -> {
            String norm = invocation.getArgument(0);
            return demoStore.stream()
                    .filter(item -> item.getNormalizedTitle().equalsIgnoreCase(norm))
                    .findFirst();
        });

        when(repository.save(any(RegisteredPublicationTitle.class))).thenAnswer(invocation -> {
            RegisteredPublicationTitle entity = invocation.getArgument(0);
            demoStore.add(entity);
            return entity;
        });

        when(repository.findByNormalizedTitleContaining(anyString())).thenAnswer(invocation -> {
            String fragment = ((String) invocation.getArgument(0)).toLowerCase();
            return demoStore.stream()
                    .filter(item -> item.getNormalizedTitle().toLowerCase().contains(fragment))
                    .collect(Collectors.toList());
        });

        when(repository.searchCandidatesByKeyword(anyString())).thenAnswer(invocation -> {
            String kw = ((String) invocation.getArgument(0)).toLowerCase();
            return demoStore.stream()
                    .filter(item -> item.getNormalizedTitle().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        });

        // Seed demo data into demoStore using RegistryDataSeeder
        TitleNormalizationService normalizationService = new TitleNormalizationService();
        RegistryDataSeeder seeder = new RegistryDataSeeder(repository, normalizationService);
        seeder.run();

        // Wire up VerificationService with Development embedding provider for standard offline unit test
        ExactMatchService exactMatchService = new ExactMatchService(repository);
        CandidateRetrievalService candidateRetrievalService = new CandidateRetrievalService(repository);
        FuzzySimilarityService fuzzySimilarityService = new FuzzySimilarityService();
        PhoneticSimilarityService phoneticSimilarityService = new PhoneticSimilarityService(normalizationService, fuzzySimilarityService);
        EmbeddingService embeddingService = new DevelopmentEmbeddingService(normalizationService);
        CosineSimilarityCalculator cosineSimilarityCalculator = new CosineSimilarityCalculator();
        SemanticSimilarityService semanticSimilarityService = new SemanticSimilarityService(embeddingService, cosineSimilarityCalculator);

        RuleEngineService ruleEngineService = new RuleEngineService(List.of(
                new ProhibitedWordRule("police,crime,corruption,cbi,cid,army"),
                new PeriodicityModifierRule("daily,weekly,fortnightly,monthly,biweekly,bimonthly,quarterly,annual,yearly,evening,morning,express,gazette,bulletin"),
                new PrefixSuffixRule("the,a,an,shree,shri,new,latest,subh,real,super,prime,grand", "news,times,post,today,now,herald,standard,live,media,chronicle,press,journal,samachar,khabar,patrikar"),
                new CombinationRule(),
                new ContextRule()
        ));
        RiskScoringService riskScoringService = new RiskScoringService(100.0, 30.0, 25.0, 25.0, 40.0, 15.0, 0.0);
        DecisionEngineService decisionEngineService = new DecisionEngineService(40.0, 70.0);
        DecisionExplanationService decisionExplanationService = new DecisionExplanationService();

        verificationService = new VerificationService(
                normalizationService, exactMatchService, candidateRetrievalService,
                fuzzySimilarityService, phoneticSimilarityService, semanticSimilarityService,
                ruleEngineService, riskScoringService, decisionEngineService, decisionExplanationService
        );
    }

    @Test
    @DisplayName("SCENARIO 1 — Exact Conflict: Namaskar News vs Namaskar News -> EXACT_MATCH_FOUND & HIGH RISK")
    void testScenario1_ExactConflict() {
        String proposedTitle = "Namaskar News";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertTrue(result.isExactMatch(), "Should detect exact match for Namaskar News");
        assertEquals("Namaskar News", result.getMatchedTitle());
        assertEquals("EXACT_MATCH_FOUND", result.getDecisionStage());
        assertEquals("HIGH RISK", result.getFinalDecision());
        assertEquals(100.0, result.getRiskScore());
    }

    @Test
    @DisplayName("SCENARIO 2 — Spelling/Fuzzy Conflict: Namascar News vs Namaskar News -> High Fuzzy & Phonetic Similarity")
    void testScenario2_SpellingFuzzyConflict() {
        String proposedTitle = "Namascar News";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertFalse(result.isExactMatch(), "Namascar News should not be exact match");
        assertEquals("CANDIDATE_SIMILARITY_ANALYSIS", result.getDecisionStage());
        assertNotNull(result.getHighestFuzzySimilarity());
        assertTrue(result.getHighestFuzzySimilarity() >= 0.80, "Fuzzy similarity should be high for spelling variant");
        assertEquals("Namaskar News", result.getTopFuzzyMatch());
    }

    @Test
    @DisplayName("SCENARIO 3 — Semantic Similarity: Evening Daily Bulletin vs registered Evening Bulletin / Daily Evening News")
    void testScenario3_SemanticSimilarity() {
        String proposedTitle = "Evening Daily Bulletin";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertFalse(result.isExactMatch());
        assertNotNull(result.getHighestSemanticSimilarity(), "Semantic similarity score should be calculated");
        assertTrue(result.getHighestSemanticSimilarity() >= 0.70, "Semantic similarity should be elevated for related wording");
    }

    @Test
    @DisplayName("SCENARIO 4 — Periodicity Modifier: India News Daily vs candidate India News -> Trigger PERIODICITY_MODIFIER rule")
    void testScenario4_PeriodicityModifier() {
        String proposedTitle = "India News Daily";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertTrue(result.getRuleResults().stream().anyMatch(r -> r.getRuleId().equals("PERIODICITY_MODIFIER") && r.isTriggered()),
                "PERIODICITY_MODIFIER rule should trigger for India News Daily");
    }

    @Test
    @DisplayName("SCENARIO 5 — Prefix/Suffix: The India Samachar vs candidate India Samachar -> Trigger PREFIX_SUFFIX rule")
    void testScenario5_PrefixSuffix() {
        String proposedTitle = "The India Samachar";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertTrue(result.getRuleResults().stream().anyMatch(r -> r.getRuleId().equals("PREFIX_SUFFIX") && r.isTriggered()),
                "PREFIX_SUFFIX rule should trigger for The India Samachar");
    }

    @Test
    @DisplayName("SCENARIO 6 — Combination: Hindu Indian Express vs candidates Hindu & Indian Express -> Trigger COMBINATION rule")
    void testScenario6_Combination() {
        String proposedTitle = "Hindu Indian Express";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertTrue(result.getRuleResults().stream().anyMatch(r -> r.getRuleId().equals("COMBINATION") && r.isTriggered()),
                "COMBINATION rule should trigger for Hindu Indian Express");
    }

    @Test
    @DisplayName("SCENARIO 7 — Restricted Word: Crime India News -> Trigger PROHIBITED_WORD rule with HIGH severity")
    void testScenario7_RestrictedWord() {
        String proposedTitle = "Crime India News";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        Optional<RuleResultDto> ruleOpt = result.getRuleResults().stream()
                .filter(r -> r.getRuleId().equals("PROHIBITED_WORD"))
                .findFirst();

        assertTrue(ruleOpt.isPresent(), "PROHIBITED_WORD rule should be evaluated");
        assertTrue(ruleOpt.get().isTriggered(), "PROHIBITED_WORD rule should trigger for Crime India News");
        assertEquals("HIGH", ruleOpt.get().getSeverity().name(), "Severity should be HIGH");
    }

    @Test
    @DisplayName("SCENARIO 8 — Clean Title: Mysuru Green Agriculture -> Low Similarity & Natural Decision Engine Output")
    void testScenario8_CleanTitle() {
        String proposedTitle = "Mysuru Green Agriculture";

        TitleVerificationResultDto result = verificationService.verifyProposedTitle(proposedTitle);

        assertFalse(result.isExactMatch(), "Clean title should not match existing records exactly");
        assertTrue(result.getRuleResults().stream().noneMatch(r -> r.getSeverity() == com.titleverify.titleverify_ai.dto.RuleSeverity.HIGH),
                "Clean title should not trigger high severity rules");
        assertNotNull(result.getFinalDecision(), "Final decision should be produced by decision engine");
    }
}
