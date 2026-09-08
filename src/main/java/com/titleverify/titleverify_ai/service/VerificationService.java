package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.*;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private final TitleNormalizationService normalizationService;
    private final ExactMatchService exactMatchService;
    private final CandidateRetrievalService candidateRetrievalService;
    private final FuzzySimilarityService fuzzySimilarityService;
    private final PhoneticSimilarityService phoneticSimilarityService;
    private final SemanticSimilarityService semanticSimilarityService;
    private final RuleEngineService ruleEngineService;
    private final RiskScoringService riskScoringService;
    private final DecisionEngineService decisionEngineService;
    private final DecisionExplanationService decisionExplanationService;

    public VerificationService(TitleNormalizationService normalizationService,
                                ExactMatchService exactMatchService,
                                CandidateRetrievalService candidateRetrievalService,
                                FuzzySimilarityService fuzzySimilarityService,
                                PhoneticSimilarityService phoneticSimilarityService,
                                SemanticSimilarityService semanticSimilarityService,
                                RuleEngineService ruleEngineService,
                                RiskScoringService riskScoringService,
                                DecisionEngineService decisionEngineService,
                                DecisionExplanationService decisionExplanationService) {
        this.normalizationService = normalizationService;
        this.exactMatchService = exactMatchService;
        this.candidateRetrievalService = candidateRetrievalService;
        this.fuzzySimilarityService = fuzzySimilarityService;
        this.phoneticSimilarityService = phoneticSimilarityService;
        this.semanticSimilarityService = semanticSimilarityService;
        this.ruleEngineService = ruleEngineService;
        this.riskScoringService = riskScoringService;
        this.decisionEngineService = decisionEngineService;
        this.decisionExplanationService = decisionExplanationService;
    }

    public TitleVerificationResultDto verifyProposedTitle(String proposedTitle) {
        return verifyProposedTitle(proposedTitle, null, null, null, null, null);
    }

    public TitleVerificationResultDto verifyProposedTitle(String proposedTitle,
                                                          String publicationType,
                                                          String language,
                                                          String state,
                                                          String district,
                                                          String periodicity) {
        String normalizedProposed = normalizationService.normalize(proposedTitle);

        // Stage 1: Exact Match Check
        Optional<RegisteredPublicationTitle> exactMatchOpt = exactMatchService.findExactMatch(normalizedProposed);

        boolean isExactMatch = exactMatchOpt.isPresent();
        String matchedTitle = isExactMatch ? exactMatchOpt.get().getTitle() : null;
        String decisionStage = isExactMatch ? "EXACT_MATCH_FOUND" : "CANDIDATE_SIMILARITY_ANALYSIS";

        // Stage 2: Candidate Retrieval
        List<RegisteredPublicationTitle> candidateEntities = candidateRetrievalService.retrieveCandidates(normalizedProposed);

        // Stage 3: Multi-Signal Similarity Analysis (Fuzzy + Phonetic + REAL Gemini Semantic)
        List<CandidateMatchDto> candidateMatches = new ArrayList<>();
        for (RegisteredPublicationTitle candidate : candidateEntities) {
            String candidateNorm = candidate.getNormalizedTitle();
            if (candidateNorm == null || candidateNorm.isEmpty()) {
                candidateNorm = normalizationService.normalize(candidate.getTitle());
            }

            // 1. Fuzzy Levenshtein Similarity
            double fuzzyScore = fuzzySimilarityService.calculateSimilarity(normalizedProposed, candidateNorm);
            String fuzzyLevel = fuzzySimilarityService.classifySimilarityLevel(fuzzyScore);

            // 2. Phonetic Metaphone Similarity
            double phoneticScore = phoneticSimilarityService.calculatePhoneticSimilarity(proposedTitle, candidate.getTitle());
            String phoneticLevel = phoneticSimilarityService.classifyPhoneticLevel(phoneticScore);

            // 3. Semantic Vector Cosine Similarity (Real Gemini API Integration)
            Double semanticScore = null;
            String semanticLevel = "UNAVAILABLE";
            OptionalDouble semOpt = semanticSimilarityService.calculateSemanticSimilarity(proposedTitle, candidate.getTitle());
            if (semOpt.isPresent()) {
                semanticScore = semOpt.getAsDouble();
                semanticLevel = semanticSimilarityService.classifySemanticLevel(semanticScore);
            }

            candidateMatches.add(new CandidateMatchDto(
                    candidate.getTitle(),
                    candidateNorm,
                    fuzzyScore,
                    fuzzyLevel,
                    phoneticScore,
                    phoneticLevel,
                    semanticScore,
                    semanticLevel
            ));
        }

        // Rank candidates by highest peak multi-signal score (max of fuzzy, phonetic, or semantic)
        candidateMatches.sort(Comparator.comparingDouble((CandidateMatchDto c) -> {
            double maxScore = Math.max(c.getSimilarityScore(), c.getPhoneticSimilarityScore());
            if (c.getSemanticSimilarityScore() != null) {
                maxScore = Math.max(maxScore, c.getSemanticSimilarityScore());
            }
            return maxScore;
        }).reversed());

        List<String> candidateTitles = candidateMatches.stream()
                .map(CandidateMatchDto::getCandidateTitle)
                .distinct()
                .collect(Collectors.toList());

        Double highestFuzzySimilarity = candidateMatches.isEmpty() ? 0.0 :
                candidateMatches.stream().mapToDouble(CandidateMatchDto::getSimilarityScore).max().orElse(0.0);

        String topFuzzyMatch = candidateMatches.stream()
                .max(Comparator.comparingDouble(CandidateMatchDto::getSimilarityScore))
                .map(CandidateMatchDto::getCandidateTitle).orElse(null);

        Double highestPhoneticSimilarity = candidateMatches.isEmpty() ? 0.0 :
                candidateMatches.stream().mapToDouble(CandidateMatchDto::getPhoneticSimilarityScore).max().orElse(0.0);

        String topPhoneticMatch = candidateMatches.stream()
                .max(Comparator.comparingDouble(CandidateMatchDto::getPhoneticSimilarityScore))
                .map(CandidateMatchDto::getCandidateTitle).orElse(null);

        Double highestSemanticSimilarity = candidateMatches.isEmpty() ? null :
                candidateMatches.stream()
                        .filter(c -> c.getSemanticSimilarityScore() != null)
                        .mapToDouble(CandidateMatchDto::getSemanticSimilarityScore)
                        .max()
                        .isPresent() ?
                        candidateMatches.stream()
                                .filter(c -> c.getSemanticSimilarityScore() != null)
                                .mapToDouble(CandidateMatchDto::getSemanticSimilarityScore)
                                .max().getAsDouble() : null;

        String topSemanticMatch = candidateMatches.stream()
                .filter(c -> c.getSemanticSimilarityScore() != null)
                .max(Comparator.comparingDouble(CandidateMatchDto::getSemanticSimilarityScore))
                .map(CandidateMatchDto::getCandidateTitle).orElse(null);

        // Stage 4: Deterministic Rule Engine Evaluation
        RuleEngineInput ruleInput = new RuleEngineInput(
                proposedTitle,
                normalizedProposed,
                publicationType,
                language,
                state,
                district,
                periodicity,
                candidateTitles
        );
        List<RuleResultDto> ruleResults = ruleEngineService != null ? ruleEngineService.evaluateRules(ruleInput) : List.of();

        // Stage 5: Risk Scoring Engine
        RiskScoreResultDto riskScoreResult = riskScoringService != null ?
                riskScoringService.calculateRiskScore(isExactMatch, highestFuzzySimilarity, highestPhoneticSimilarity, highestSemanticSimilarity, ruleResults)
                : new RiskScoreResultDto(0.0, "LOW RISK", 100.0);

        // Stage 6: Decision Engine
        VerificationDecision decision = decisionEngineService != null ?
                decisionEngineService.determineDecision(isExactMatch, riskScoreResult, ruleResults)
                : (isExactMatch ? VerificationDecision.HIGH_RISK : VerificationDecision.ACCEPT);

        // Stage 7: Decision Explanation Service
        String closestCandidate = !candidateTitles.isEmpty() ? candidateTitles.get(0) : (matchedTitle != null ? matchedTitle : null);
        DecisionExplanationDto explanation = decisionExplanationService != null ?
                decisionExplanationService.generateExplanation(
                        proposedTitle, isExactMatch, matchedTitle, closestCandidate,
                        highestFuzzySimilarity, highestPhoneticSimilarity, highestSemanticSimilarity,
                        ruleResults, riskScoreResult, decision
                ) : new DecisionExplanationDto(decision.getCode(), riskScoreResult.getRiskScore(), riskScoreResult.getRiskLevel(), riskScoreResult.getApprovalConfidence(), closestCandidate, List.of(), "");

        return new TitleVerificationResultDto(
                proposedTitle,
                normalizedProposed,
                isExactMatch,
                matchedTitle,
                candidateTitles,
                decisionStage,
                candidateMatches,
                highestFuzzySimilarity,
                topFuzzyMatch,
                highestPhoneticSimilarity,
                topPhoneticMatch,
                highestSemanticSimilarity,
                topSemanticMatch,
                ruleResults,
                explanation.getFinalDecision(),
                explanation.getRiskScore(),
                explanation.getRiskLevel(),
                explanation.getApprovalConfidence(),
                explanation.getClosestMatch(),
                explanation.getReasons(),
                explanation.getRecommendation()
        );
    }

    public TitleNormalizationService getNormalizationService() {
        return normalizationService;
    }

    public FuzzySimilarityService getFuzzySimilarityService() {
        return fuzzySimilarityService;
    }

    public PhoneticSimilarityService getPhoneticSimilarityService() {
        return phoneticSimilarityService;
    }

    public SemanticSimilarityService getSemanticSimilarityService() {
        return semanticSimilarityService;
    }

    public RuleEngineService getRuleEngineService() {
        return ruleEngineService;
    }

    public RiskScoringService getRiskScoringService() {
        return riskScoringService;
    }

    public DecisionEngineService getDecisionEngineService() {
        return decisionEngineService;
    }

    public DecisionExplanationService getDecisionExplanationService() {
        return decisionExplanationService;
    }
}
