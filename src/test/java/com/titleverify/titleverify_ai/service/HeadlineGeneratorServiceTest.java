package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.CandidateMatchDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationRequestDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationResponseDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeadlineGeneratorServiceTest {

    private GeminiHeadlineGenerationService geminiService;
    private VerificationService verificationService;
    private ContentRelevanceService contentRelevanceService;
    private HeadlineRecommendationService recommendationService;
    private HeadlineGeneratorService headlineGeneratorService;

    @BeforeEach
    void setUp() {
        geminiService = Mockito.mock(GeminiHeadlineGenerationService.class);
        verificationService = Mockito.mock(VerificationService.class);
        contentRelevanceService = new ContentRelevanceService();
        recommendationService = new HeadlineRecommendationService();

        headlineGeneratorService = new HeadlineGeneratorService(
                geminiService,
                verificationService,
                contentRelevanceService,
                recommendationService
        );
    }

    @Test
    void testGenerateAndVerifyHeadlinesEndToEndSuccessWithContext() {
        String article = "Karnataka government launches digital media initiative for news publication verification.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                article, "Newspaper", "English", "Karnataka", "Bengaluru Urban", "Daily"
        );

        List<String> mockHeadlines = List.of(
                "Karnataka Digital Media Initiative Launched",
                "State Announces News Verification Portal",
                "Digital Publication Standards Introduced",
                "New Media Guidelines For Registration",
                "State Digital News Framework Approved"
        );
        when(geminiService.generateHeadlines(anyString(), anyString())).thenReturn(mockHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(10.0);
        mockV.setRiskLevel("LOW RISK");
        mockV.setApprovalConfidence(90.0);
        when(verificationService.verifyProposedTitle(anyString(), eq("Newspaper"), eq("English"), eq("Karnataka"), eq("Bengaluru Urban"), eq("Daily")))
                .thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertNotNull(response.getRecommendedHeadline());
        assertTrue(response.getRecommendationReason().contains("relevance"));

        // Verify every generated headline entered VerificationService with complete context
        for (String h : mockHeadlines) {
            verify(verificationService).verifyProposedTitle(h, "Newspaper", "English", "Karnataka", "Bengaluru Urban", "Daily");
        }
    }

    @Test
    void testMysuruArticleManualTestScenario() {
        String mysuruArticle = """
                Mysuru, September 1: The Mysuru City Corporation has launched a new
                waste segregation awareness programme across several residential
                areas in the city. The programme aims to encourage households to
                separate wet and dry waste before handing it over to collection
                workers.

                The initiative was introduced following a review of waste-management
                practices in different parts of Mysuru. Officials said improper
                mixing of wet and dry waste has increased the amount of waste
                requiring additional sorting and processing.
                """;

        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                mysuruArticle, "Newspaper", "English", "Karnataka", "Mysuru", "Daily"
        );

        List<String> mockHeadlines = List.of(
                "Mysuru City Corporation Launches Waste Segregation Drive",
                "Household Wet and Dry Waste Separation Campaign in Mysuru",
                "Mysuru Waste Management Review Prompts New Recycling Initiative",
                "City Corporation Enlists Community Support for Segregation",
                "Mysuru Civic Body Urges Resident Cooperation on Waste Guidelines"
        );
        when(geminiService.generateHeadlines(anyString(), anyString())).thenReturn(mockHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(12.0);
        mockV.setRiskLevel("LOW RISK");
        mockV.setApprovalConfidence(88.0);
        when(verificationService.verifyProposedTitle(anyString(), eq("Newspaper"), eq("English"), eq("Karnataka"), eq("Mysuru"), eq("Daily")))
                .thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertNotNull(response.getRecommendedHeadline());
        assertEquals("ACCEPT", response.getRecommendedHeadline().getVerificationResult().getFinalDecision());
        assertEquals("LOW RISK", response.getRecommendedHeadline().getVerificationResult().getRiskLevel());
    }

    @Test
    void testSemanticMatchDisplayStateCases() {
        // Case A: No candidate title available for comparison
        TitleVerificationResultDto vCaseA = new TitleVerificationResultDto();
        vCaseA.setCandidateMatches(List.of());
        com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto itemCaseA =
                new com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto("Headline A", 80.0, vCaseA);
        assertEquals("No comparable registered title found", itemCaseA.getSemanticMatchDisplay());

        // Case B: Candidates exist but semantic analysis vector failed / null
        TitleVerificationResultDto vCaseB = new TitleVerificationResultDto();
        CandidateMatchDto cand = new CandidateMatchDto("Registered Title", "registered title", 0.5, "MODERATE", 0.5, "MODERATE", null, "UNAVAILABLE");
        vCaseB.setCandidateMatches(List.of(cand));
        vCaseB.setHighestSemanticSimilarity(null);
        com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto itemCaseB =
                new com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto("Headline B", 80.0, vCaseB);
        assertEquals("Semantic analysis unavailable", itemCaseB.getSemanticMatchDisplay());

        // Case C: Real semantic similarity value present
        TitleVerificationResultDto vCaseC = new TitleVerificationResultDto();
        vCaseC.setCandidateMatches(List.of(cand));
        vCaseC.setHighestSemanticSimilarity(0.452);
        com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto itemCaseC =
                new com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto("Headline C", 80.0, vCaseC);
        assertEquals("45.2%", itemCaseC.getSemanticMatchDisplay());
    }

    @Test
    void testPartialVerificationFailureContinuesProcessing() {
        String article = "State government approves new energy project in Bengaluru with major investments.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(article);

        List<String> mockHeadlines = List.of(
                "Bengaluru Energy Project Approved",
                "State Investment In Renewable Energy",
                "Headline 3 With Verification Error",
                "New Clean Energy Hub Opened",
                "Bengaluru Power Sector Boosted"
        );
        when(geminiService.generateHeadlines(anyString(), anyString())).thenReturn(mockHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(15.0);

        when(verificationService.verifyProposedTitle(eq("Bengaluru Energy Project Approved"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockV);
        when(verificationService.verifyProposedTitle(eq("State Investment In Renewable Energy"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockV);
        when(verificationService.verifyProposedTitle(eq("Headline 3 With Verification Error"), anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Database timeout"));
        when(verificationService.verifyProposedTitle(eq("New Clean Energy Hub Opened"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockV);
        when(verificationService.verifyProposedTitle(eq("Bengaluru Power Sector Boosted"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("PARTIAL_SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertTrue(response.getResults().get(2).isVerificationError());
        assertFalse(response.getResults().get(0).isVerificationError());
        assertNotNull(response.getRecommendedHeadline());
    }

    @Test
    void testEmptyInputReturnsErrorResponse() {
        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(new HeadlineGenerationRequestDto(""));
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getMessage().contains("empty"));
    }
}
