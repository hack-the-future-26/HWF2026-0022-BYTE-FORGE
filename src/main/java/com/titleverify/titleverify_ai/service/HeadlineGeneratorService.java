package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.HeadlineGenerationRequestDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationResponseDto;
import com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeadlineGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(HeadlineGeneratorService.class);

    private final GeminiHeadlineGenerationService headlineGenerationService;
    private final VerificationService verificationService;
    private final ContentRelevanceService contentRelevanceService;
    private final HeadlineRecommendationService recommendationService;

    public HeadlineGeneratorService(GeminiHeadlineGenerationService headlineGenerationService,
                                    VerificationService verificationService,
                                    ContentRelevanceService contentRelevanceService,
                                    HeadlineRecommendationService recommendationService) {
        this.headlineGenerationService = headlineGenerationService;
        this.verificationService = verificationService;
        this.contentRelevanceService = contentRelevanceService;
        this.recommendationService = recommendationService;
    }

    public HeadlineGenerationResponseDto generateAndVerifyHeadlines(HeadlineGenerationRequestDto requestDto) {
        if (requestDto == null || requestDto.getContent() == null || requestDto.getContent().trim().isEmpty()) {
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "News topic or article content cannot be empty.");
        }

        String content = requestDto.getContent().trim();
        if (content.length() < 10) {
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "Content is too short. Please provide at least 10 characters.");
        }
        if (content.length() > 5000) {
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "Content exceeds maximum length of 5000 characters.");
        }

        // Step 1 & 2: Generate 5 Headlines via Gemini
        List<String> rawHeadlines;
        try {
            rawHeadlines = headlineGenerationService.generateHeadlines(content, requestDto.getLanguage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Headline generation failed: {}", e.getMessage());
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "Failed to generate headlines. Please verify AI API configuration and try again.");
        }

        if (rawHeadlines == null || rawHeadlines.isEmpty()) {
            return new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "Headline generation returned no valid options.");
        }

        // Step 3 & 4: Verify each headline using existing VerificationService & compute Content Relevance
        List<HeadlineVerificationResultDto> verifiedResults = new ArrayList<>();
        int verificationFailures = 0;

        for (String headline : rawHeadlines) {
            double relevance = contentRelevanceService.calculateRelevance(headline, content);

            try {
                TitleVerificationResultDto verification = verificationService.verifyProposedTitle(
                        headline,
                        requestDto.getPublicationType(),
                        requestDto.getLanguage(),
                        requestDto.getState(),
                        requestDto.getDistrict(),
                        requestDto.getPeriodicity()
                );
                verifiedResults.add(new HeadlineVerificationResultDto(headline, relevance, verification));
            } catch (Exception e) {
                logger.error("Individual title verification failed for headline '{}': {}", headline, e.getMessage());
                verificationFailures++;
                verifiedResults.add(new HeadlineVerificationResultDto(headline, relevance, "Title verification unavailable due to system processing error."));
            }
        }

        if (verifiedResults.size() < 5) {
            logger.warn("Received {} headlines instead of 5.", verifiedResults.size());
        }

        // Step 5: Select Recommended Headline
        HeadlineVerificationResultDto recommendedHeadline = recommendationService.selectBestHeadline(verifiedResults);
        String recommendationReason = recommendationService.generateRecommendationExplanation(recommendedHeadline);

        String status = (verificationFailures > 0 && verificationFailures < verifiedResults.size()) ? "PARTIAL_SUCCESS" : "SUCCESS";
        String message = (verificationFailures > 0)
                ? String.format("Successfully processed %d headline options (%d verification step(s) encountered an issue).", verifiedResults.size(), verificationFailures)
                : "Successfully generated and verified 5 headline options.";

        return new HeadlineGenerationResponseDto(
                verifiedResults,
                recommendedHeadline,
                recommendationReason,
                status,
                message
        );
    }
}
