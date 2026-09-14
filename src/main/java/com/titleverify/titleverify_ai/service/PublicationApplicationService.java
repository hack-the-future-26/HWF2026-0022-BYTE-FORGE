package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.ApplicationRequestDto;
import com.titleverify.titleverify_ai.dto.ApplicationResponseDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.dto.VerificationHistoryItemDto;
import com.titleverify.titleverify_ai.entity.PublicationApplication;
import com.titleverify.titleverify_ai.entity.ProposedTitle;
import com.titleverify.titleverify_ai.repository.PublicationApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublicationApplicationService {

    private final PublicationApplicationRepository applicationRepository;
    private final VerificationService verificationService;

    public PublicationApplicationService(PublicationApplicationRepository applicationRepository,
                                         VerificationService verificationService) {
        this.applicationRepository = applicationRepository;
        this.verificationService = verificationService;
    }

    @Transactional
    public ApplicationResponseDto saveApplication(ApplicationRequestDto dto) {
        validateRequest(dto);

        PublicationApplication application = new PublicationApplication(
                dto.getPublicationType().trim(),
                dto.getLanguage().trim(),
                dto.getState().trim(),
                dto.getDistrict().trim(),
                dto.getPeriodicity().trim()
        );

        List<String> titles = dto.getProposedTitles();
        List<TitleVerificationResultDto> verificationResults = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            String titleText = titles.get(i) != null ? titles.get(i).trim() : "";
            if (!titleText.isEmpty()) {
                ProposedTitle proposedTitle = new ProposedTitle(titleText, i + 1);

                TitleVerificationResultDto verificationResult = verificationService.verifyProposedTitle(
                        titleText,
                        application.getPublicationType(),
                        application.getLanguage(),
                        application.getState(),
                        application.getDistrict(),
                        application.getPeriodicity()
                );
                verificationResults.add(verificationResult);

                proposedTitle.setNormalizedTitle(verificationResult.getNormalizedTitle());
                proposedTitle.setExactMatch(verificationResult.isExactMatch());
                proposedTitle.setMatchedTitle(verificationResult.getMatchedTitle());
                proposedTitle.setVerificationStage(verificationResult.getDecisionStage());

                application.addProposedTitle(proposedTitle);
            }
        }

        PublicationApplication savedApplication = applicationRepository.save(application);

        return new ApplicationResponseDto(
                savedApplication.getId(),
                "SUCCESS",
                "Your title options have been captured successfully.",
                savedApplication.getCreatedAt(),
                verificationResults
        );
    }

    @Transactional(readOnly = true)
    public ApplicationAnalysisDetailsDto getApplicationDetails(Long applicationId) {
        if (applicationId == null) {
            throw new IllegalArgumentException("Application ID cannot be null.");
        }
        PublicationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        List<TitleVerificationResultDto> verificationResults = new ArrayList<>();
        int lowRiskCount = 0;
        int reviewCount = 0;
        int highRiskCount = 0;

        for (ProposedTitle pt : application.getProposedTitles()) {
            if (pt.getTitle() != null && !pt.getTitle().trim().isEmpty()) {
                TitleVerificationResultDto result = verificationService.verifyProposedTitle(
                        pt.getTitle().trim(),
                        application.getPublicationType(),
                        application.getLanguage(),
                        application.getState(),
                        application.getDistrict(),
                        application.getPeriodicity()
                );
                verificationResults.add(result);

                if ("ACCEPT".equalsIgnoreCase(result.getFinalDecision())) {
                    lowRiskCount++;
                } else if ("REVIEW".equalsIgnoreCase(result.getFinalDecision())) {
                    reviewCount++;
                } else {
                    highRiskCount++;
                }
            }
        }

        return new ApplicationAnalysisDetailsDto(
                application.getId(),
                application.getPublicationType(),
                application.getLanguage(),
                application.getState(),
                application.getDistrict(),
                application.getPeriodicity(),
                application.getCreatedAt(),
                verificationResults,
                verificationResults.size(),
                lowRiskCount,
                reviewCount,
                highRiskCount
        );
    }

    @Transactional(readOnly = true)
    public List<VerificationHistoryItemDto> getHistorySummaries() {
        List<PublicationApplication> applications = applicationRepository.findAllByOrderByCreatedAtDesc();
        List<VerificationHistoryItemDto> historyList = new ArrayList<>();

        for (PublicationApplication app : applications) {
            List<String> titles = new ArrayList<>();
            int lowRisk = 0;
            int review = 0;
            int highRisk = 0;

            for (ProposedTitle pt : app.getProposedTitles()) {
                if (pt.getTitle() != null && !pt.getTitle().trim().isEmpty()) {
                    titles.add(pt.getTitle().trim());

                    TitleVerificationResultDto result = verificationService.verifyProposedTitle(
                            pt.getTitle().trim(),
                            app.getPublicationType(),
                            app.getLanguage(),
                            app.getState(),
                            app.getDistrict(),
                            app.getPeriodicity()
                    );

                    if ("ACCEPT".equalsIgnoreCase(result.getFinalDecision())) {
                        lowRisk++;
                    } else if ("REVIEW".equalsIgnoreCase(result.getFinalDecision())) {
                        review++;
                    } else {
                        highRisk++;
                    }
                }
            }

            String overallDecision;
            if (highRisk > 0) {
                overallDecision = "HIGH RISK";
            } else if (review > 0) {
                overallDecision = "REVIEW";
            } else if (lowRisk > 0) {
                overallDecision = "ACCEPT";
            } else {
                overallDecision = "PENDING";
            }

            historyList.add(new VerificationHistoryItemDto(
                    app.getId(),
                    app.getCreatedAt(),
                    app.getPublicationType(),
                    app.getLanguage(),
                    app.getState(),
                    app.getDistrict(),
                    app.getPeriodicity(),
                    titles.size(),
                    titles,
                    overallDecision
            ));
        }

        return historyList;
    }

    @Transactional(readOnly = true)
    public TitleComparisonDto getComparisonDetails(Long applicationId) {
        ApplicationAnalysisDetailsDto details = getApplicationDetails(applicationId);
        List<TitleVerificationResultDto> verificationResults = details.getVerificationResults();

        if (verificationResults == null || verificationResults.isEmpty()) {
            return new TitleComparisonDto(
                    details.getApplicationId(),
                    details.getPublicationType(),
                    details.getLanguage(),
                    details.getState(),
                    details.getDistrict(),
                    details.getPeriodicity(),
                    details.getCreatedAt(),
                    new ArrayList<>(),
                    null,
                    "No title verification results available for comparison."
            );
        }

        int bestIndex = 0;
        TitleVerificationResultDto bestTitle = verificationResults.get(0);

        for (int i = 1; i < verificationResults.size(); i++) {
            TitleVerificationResultDto candidate = verificationResults.get(i);
            if (compareCandidates(candidate, i, bestTitle, bestIndex) < 0) {
                bestTitle = candidate;
                bestIndex = i;
            }
        }

        String explanation = generateRecommendationExplanation(bestTitle, bestIndex + 1, verificationResults.size());

        return new TitleComparisonDto(
                details.getApplicationId(),
                details.getPublicationType(),
                details.getLanguage(),
                details.getState(),
                details.getDistrict(),
                details.getPeriodicity(),
                details.getCreatedAt(),
                verificationResults,
                bestTitle,
                explanation
        );
    }

    private int compareCandidates(TitleVerificationResultDto a, int indexA, TitleVerificationResultDto b, int indexB) {
        int rankA = getDecisionRank(a.getFinalDecision());
        int rankB = getDecisionRank(b.getFinalDecision());
        if (rankA != rankB) {
            return Integer.compare(rankA, rankB);
        }

        double riskA = a.getRiskScore() != null ? a.getRiskScore() : 100.0;
        double riskB = b.getRiskScore() != null ? b.getRiskScore() : 100.0;
        if (Double.compare(riskA, riskB) != 0) {
            return Double.compare(riskA, riskB);
        }

        long rulesA = countTriggeredRules(a);
        long rulesB = countTriggeredRules(b);
        if (rulesA != rulesB) {
            return Long.compare(rulesA, rulesB);
        }

        double maxSimA = getMaxSimilarity(a);
        double maxSimB = getMaxSimilarity(b);
        if (Double.compare(maxSimA, maxSimB) != 0) {
            return Double.compare(maxSimA, maxSimB);
        }

        return Integer.compare(indexA, indexB);
    }

    private int getDecisionRank(String decision) {
        if (decision == null) return 4;
        if ("ACCEPT".equalsIgnoreCase(decision)) return 1;
        if ("REVIEW".equalsIgnoreCase(decision)) return 2;
        if ("HIGH RISK".equalsIgnoreCase(decision) || "HIGH_RISK".equalsIgnoreCase(decision)) return 3;
        return 4;
    }

    private long countTriggeredRules(TitleVerificationResultDto result) {
        if (result.getRuleResults() == null) return 0;
        return result.getRuleResults().stream().filter(RuleResultDto::isTriggered).count();
    }

    private double getMaxSimilarity(TitleVerificationResultDto result) {
        double max = 0.0;
        if (result.getHighestFuzzySimilarity() != null) {
            max = Math.max(max, result.getHighestFuzzySimilarity());
        }
        if (result.getHighestPhoneticSimilarity() != null) {
            max = Math.max(max, result.getHighestPhoneticSimilarity());
        }
        if (result.getHighestSemanticSimilarity() != null) {
            max = Math.max(max, result.getHighestSemanticSimilarity());
        }
        if (result.getHighestBm25Similarity() != null) {
            max = Math.max(max, result.getHighestBm25Similarity());
        }
        return max;
    }

    private String generateRecommendationExplanation(TitleVerificationResultDto title, int optionNumber, int totalOptions) {
        if (title == null) {
            return "No recommendation available.";
        }

        String titleText = title.getProposedTitle() != null ? title.getProposedTitle() : "Option #" + optionNumber;
        String decision = title.getFinalDecision() != null ? title.getFinalDecision().toUpperCase() : "UNKNOWN";
        String riskStr = title.getRiskScore() != null ? String.format("%.1f", title.getRiskScore()) : "N/A";
        long ruleCount = countTriggeredRules(title);

        if (totalOptions <= 1) {
            return String.format("Option #%d ('%s') is the sole submitted title with an overall decision of %s (Risk Score: %s).",
                    optionNumber, titleText, decision, riskStr);
        }

        if ("ACCEPT".equalsIgnoreCase(decision)) {
            if (ruleCount == 0) {
                return String.format("Option #%d ('%s') is recommended as the safest choice with an ACCEPT decision, the lowest risk score (%s) among submitted options, and no rule conflicts.",
                        optionNumber, titleText, riskStr);
            } else {
                return String.format("Option #%d ('%s') is recommended as the safest choice with an ACCEPT decision and the lowest risk score (%s) among submitted options.",
                        optionNumber, titleText, riskStr);
            }
        } else if ("REVIEW".equalsIgnoreCase(decision)) {
            return String.format("Option #%d ('%s') is recommended as the most viable option among the submitted titles with a REVIEW status and a risk score of %s.",
                    optionNumber, titleText, riskStr);
        } else {
            return String.format("Option #%d ('%s') is the least conflicting option among the submitted titles, though all options carry a HIGH RISK classification (Risk Score: %s).",
                    optionNumber, titleText, riskStr);
        }
    }

    private void validateRequest(ApplicationRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Application request payload cannot be null.");
        }
        if (isEmpty(dto.getPublicationType())) {
            throw new IllegalArgumentException("Publication type is required.");
        }
        if (isEmpty(dto.getLanguage())) {
            throw new IllegalArgumentException("Language is required.");
        }
        if (isEmpty(dto.getState())) {
            throw new IllegalArgumentException("State is required.");
        }
        if (isEmpty(dto.getDistrict())) {
            throw new IllegalArgumentException("District is required.");
        }
        if (isEmpty(dto.getPeriodicity())) {
            throw new IllegalArgumentException("Periodicity is required.");
        }
        if (dto.getProposedTitles() == null || dto.getProposedTitles().isEmpty()) {
            throw new IllegalArgumentException("At least one proposed title is required.");
        }
        long nonBlankCount = dto.getProposedTitles().stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .count();
        if (nonBlankCount == 0) {
            throw new IllegalArgumentException("At least one valid proposed title is required.");
        }
        if (dto.getProposedTitles().size() > 5) {
            throw new IllegalArgumentException("Maximum of 5 proposed titles allowed.");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
