package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.ApplicationRequestDto;
import com.titleverify.titleverify_ai.dto.ApplicationResponseDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
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

                // Perform verification for proposed title with application context
                TitleVerificationResultDto verificationResult = verificationService.verifyProposedTitle(
                        titleText,
                        application.getPublicationType(),
                        application.getLanguage(),
                        application.getState(),
                        application.getDistrict(),
                        application.getPeriodicity()
                );
                verificationResults.add(verificationResult);

                // Store verification outcomes on the entity
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
