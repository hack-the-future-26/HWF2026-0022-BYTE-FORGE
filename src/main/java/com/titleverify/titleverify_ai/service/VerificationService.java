package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.*;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.rule.RuleEngineInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final TitleNormalizationService normalizationService;
    private final ExactMatchService exactMatchService;
    private final CandidateRetrievalService candidateRetrievalService;
    private final FuzzySimilarityService fuzzySimilarityService;
    private final PhoneticSimilarityService phoneticSimilarityService;
    private final SemanticSimilarityService semanticSimilarityService;
    private final Bm25SimilarityService bm25SimilarityService;
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
        this(normalizationService, exactMatchService, candidateRetrievalService,
                fuzzySimilarityService, phoneticSimilarityService, semanticSimilarityService,
                null,
                ruleEngineService, riskScoringService, decisionEngineService, decisionExplanationService);
    }

    @Autowired
    public VerificationService(TitleNormalizationService normalizationService,
                               ExactMatchService exactMatchService,
                               CandidateRetrievalService candidateRetrievalService,
                               FuzzySimilarityService fuzzySimilarityService,
                               PhoneticSimilarityService phoneticSimilarityService,
                               SemanticSimilarityService semanticSimilarityService,
                               @Autowired(required = false) Bm25SimilarityService bm25SimilarityService,
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
        this.bm25SimilarityService = bm25SimilarityService != null ? bm25SimilarityService : new Bm25SimilarityService(normalizationService);
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
        String normalizedProposed = normalizationService != null ? normalizationService.normalize(proposedTitle) : "";

        // Check for empty, blank, or invalid title inputs lacking alphanumeric characters
        if (proposedTitle == null || proposedTitle.trim().isEmpty() || normalizedProposed.isEmpty()) {
            String displayTitle = (proposedTitle != null) ? proposedTitle : "";
            String invalidReason = (proposedTitle == null || proposedTitle.trim().isEmpty())
                    ? "Proposed title is empty or contains no characters."
                    : "Proposed title '" + proposedTitle + "' contains no valid alphanumeric characters in any supported language.";
            String recommendation = "Please provide a valid publication title containing alphabetic or alphanumeric characters.";

            List<RuleResultDto> validationRules = List.of(new RuleResultDto(
                    "TITLE_FORMAT_VALIDATION",
                    "Title Format Validation",
                    RuleSeverity.HIGH,
                    true,
                    invalidReason,
                    displayTitle,
                    recommendation
            ));

            return new TitleVerificationResultDto(
                    displayTitle,
                    normalizedProposed,
                    false,
                    null,
                    List.of(),
                    "INVALID_TITLE_INPUT",
                    List.of(),
                    0.0,
                    null,
                    0.0,
                    null,
                    null,
                    null,
                    validationRules,
                    VerificationDecision.HIGH_RISK.getCode(),
                    100.0,
                    "HIGH RISK",
                    0.0,
                    null,
                    List.of(invalidReason),
                    recommendation
            );
        }

        try {
            // Stage 1: Exact Match Check
            Optional<RegisteredPublicationTitle> exactMatchOpt = exactMatchService != null
                    ? exactMatchService.findExactMatch(normalizedProposed)
                    : Optional.empty();

            boolean isExactMatch = exactMatchOpt.isPresent();
            String matchedTitle = isExactMatch ? exactMatchOpt.get().getTitle() : null;
            String decisionStage = isExactMatch ? "EXACT_MATCH_FOUND" : "CANDIDATE_SIMILARITY_ANALYSIS";

            // Stage 2: Candidate Retrieval
            List<RegisteredPublicationTitle> candidateEntities = candidateRetrievalService != null
                    ? candidateRetrievalService.retrieveCandidates(normalizedProposed)
                    : new ArrayList<>();

            // Stage 3: Candidate Similarity Analysis
            List<CandidateMatchDto> candidateMatches = new ArrayList<>();
            for (RegisteredPublicationTitle candidate : candidateEntities) {
                if (candidate == null) continue;
                String candidateTitle = candidate.getTitle() != null ? candidate.getTitle() : "";
                String candidateNorm = candidate.getNormalizedTitle();
                if (candidateNorm == null || candidateNorm.isEmpty()) {
                    candidateNorm = normalizationService != null ? normalizationService.normalize(candidateTitle) : candidateTitle.toLowerCase();
                }

                double fuzzyScore = fuzzySimilarityService != null ? fuzzySimilarityService.calculateSimilarity(normalizedProposed, candidateNorm) : 0.0;
                String fuzzyLevel = fuzzySimilarityService != null ? fuzzySimilarityService.classifySimilarityLevel(fuzzyScore) : "LOW";

                double phoneticScore = phoneticSimilarityService != null ? phoneticSimilarityService.calculatePhoneticSimilarity(proposedTitle, candidateTitle) : 0.0;
                String phoneticLevel = phoneticSimilarityService != null ? phoneticSimilarityService.classifyPhoneticLevel(phoneticScore) : "LOW";

                Double semanticScore = null;
                String semanticLevel = "UNAVAILABLE";
                if (semanticSimilarityService != null) {
                    try {
                        OptionalDouble semOpt = semanticSimilarityService.calculateSemanticSimilarity(proposedTitle, candidateTitle);
                        if (semOpt.isPresent()) {
                            semanticScore = semOpt.getAsDouble();
                            semanticLevel = semanticSimilarityService.classifySemanticLevel(semanticScore);
                        }
                    } catch (Exception e) {
                        logger.warn("Semantic similarity computation failed for candidate '{}': {}", candidateTitle, e.getMessage());
                        semanticScore = null;
                        semanticLevel = "UNAVAILABLE";
                    }
                }

                Double bm25Score = null;
                String bm25Level = "LOW";
                if (bm25SimilarityService != null) {
                    try {
                        bm25Score = bm25SimilarityService.calculateBm25Similarity(proposedTitle, candidateTitle);
                        bm25Level = bm25SimilarityService.classifyBm25Level(bm25Score);
                    } catch (Exception e) {
                        logger.warn("BM25 similarity computation failed for candidate '{}': {}", candidateTitle, e.getMessage());
                        bm25Score = 0.0;
                        bm25Level = "LOW";
                    }
                }

                candidateMatches.add(new CandidateMatchDto(
                        candidateTitle,
                        candidateNorm,
                        fuzzyScore,
                        fuzzyLevel,
                        phoneticScore,
                        phoneticLevel,
                        semanticScore,
                        semanticLevel,
                        bm25Score,
                        bm25Level
                ));
            }

            candidateMatches.sort(Comparator.comparingDouble((CandidateMatchDto c) -> {
                double maxScore = Math.max(c.getSimilarityScore(), c.getPhoneticSimilarityScore());
                if (c.getSemanticSimilarityScore() != null) {
                    maxScore = Math.max(maxScore, c.getSemanticSimilarityScore());
                }
                if (c.getBm25SimilarityScore() != null) {
                    maxScore = Math.max(maxScore, c.getBm25SimilarityScore());
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

            Double highestSemanticSimilarity = candidateMatches.stream()
                    .map(CandidateMatchDto::getSemanticSimilarityScore)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(null);

            String topSemanticMatch = candidateMatches.stream()
                    .filter(c -> c.getSemanticSimilarityScore() != null)
                    .max(Comparator.comparingDouble(CandidateMatchDto::getSemanticSimilarityScore))
                    .map(CandidateMatchDto::getCandidateTitle).orElse(null);

            Double highestBm25Similarity = candidateMatches.stream()
                    .map(CandidateMatchDto::getBm25SimilarityScore)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(null);

            String topBm25Match = candidateMatches.stream()
                    .filter(c -> c.getBm25SimilarityScore() != null)
                    .max(Comparator.comparingDouble(CandidateMatchDto::getBm25SimilarityScore))
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

            TitleVerificationResultDto resultDto = new TitleVerificationResultDto(
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
            resultDto.setHighestBm25Similarity(highestBm25Similarity);
            resultDto.setTopBm25Match(topBm25Match);
            return resultDto;
        } catch (Exception e) {
            logger.error("Unexpected error during title verification for '{}': {}", proposedTitle, e.getMessage(), e);
            String reason = "Automated verification encountered a system error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error") + ". Flagged for manual review.";
            String rec = "An unexpected processing error occurred during automated pre-screening. Please try verifying again or request manual administrative review.";
            return new TitleVerificationResultDto(
                    proposedTitle,
                    normalizedProposed,
                    false,
                    null,
                    List.of(),
                    "VERIFICATION_DEGRADED",
                    List.of(),
                    0.0,
                    null,
                    0.0,
                    null,
                    null,
                    null,
                    List.of(),
                    VerificationDecision.REVIEW.getCode(),
                    50.0,
                    "MEDIUM RISK",
                    50.0,
                    null,
                    List.of(reason),
                    rec
            );
        }
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

    public Bm25SimilarityService getBm25SimilarityService() {
        return bm25SimilarityService;
    }
}
