package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.dto.VerificationHistoryItemDto;
import com.titleverify.titleverify_ai.entity.ProposedTitle;
import com.titleverify.titleverify_ai.entity.PublicationApplication;
import com.titleverify.titleverify_ai.repository.PublicationApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class PublicationApplicationServiceTest {

    private PublicationApplicationRepository applicationRepository;
    private VerificationService verificationService;
    private PublicationApplicationService service;

    @BeforeEach
    void setUp() {
        applicationRepository = Mockito.mock(PublicationApplicationRepository.class);
        verificationService = Mockito.mock(VerificationService.class);
        service = new PublicationApplicationService(applicationRepository, verificationService);
    }

    @Test
    @DisplayName("getHistorySummaries should return empty list when no applications exist")
    void testGetHistorySummariesEmpty() {
        when(applicationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        List<VerificationHistoryItemDto> result = service.getHistorySummaries();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getHistorySummaries should return mapped summaries with overall decision ACCEPT")
    void testGetHistorySummariesWithAccept() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(1L);
        app.setCreatedAt(LocalDateTime.of(2026, 9, 14, 10, 0));

        ProposedTitle title1 = new ProposedTitle("Delhi Chronicle", 1);
        app.addProposedTitle(title1);

        when(applicationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(app));

        TitleVerificationResultDto verificationResult = new TitleVerificationResultDto();
        verificationResult.setProposedTitle("Delhi Chronicle");
        verificationResult.setFinalDecision("ACCEPT");

        when(verificationService.verifyProposedTitle(eq("Delhi Chronicle"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(verificationResult);

        List<VerificationHistoryItemDto> result = service.getHistorySummaries();

        assertNotNull(result);
        assertEquals(1, result.size());

        VerificationHistoryItemDto item = result.get(0);
        assertEquals(1L, item.getApplicationId());
        assertEquals("Newspaper", item.getPublicationType());
        assertEquals("English", item.getLanguage());
        assertEquals("Delhi", item.getState());
        assertEquals("Central", item.getDistrict());
        assertEquals("Daily", item.getPeriodicity());
        assertEquals(1, item.getTitleCount());
        assertEquals(List.of("Delhi Chronicle"), item.getProposedTitles());
        assertEquals("ACCEPT", item.getOverallDecision());
        assertEquals("14 Sep 2026, 10:00 AM", item.getFormattedCreatedAt());
    }

    @Test
    @DisplayName("getHistorySummaries should mark overall decision as HIGH RISK if any proposed title is HIGH RISK")
    void testGetHistorySummariesWithHighRisk() {
        PublicationApplication app = new PublicationApplication("Newspaper", "Hindi", "Uttar Pradesh", "Lucknow", "Daily");
        app.setId(2L);
        app.setCreatedAt(LocalDateTime.of(2026, 9, 14, 12, 30));

        ProposedTitle title1 = new ProposedTitle("Lucknow Post", 1);
        ProposedTitle title2 = new ProposedTitle("Crime News Daily", 2);
        app.addProposedTitle(title1);
        app.addProposedTitle(title2);

        when(applicationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Lucknow Post");
        res1.setFinalDecision("ACCEPT");

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Crime News Daily");
        res2.setFinalDecision("HIGH RISK");

        when(verificationService.verifyProposedTitle(eq("Lucknow Post"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Crime News Daily"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(res2);

        List<VerificationHistoryItemDto> result = service.getHistorySummaries();

        assertEquals(1, result.size());
        VerificationHistoryItemDto item = result.get(0);
        assertEquals(2L, item.getApplicationId());
        assertEquals(2, item.getTitleCount());
        assertEquals("HIGH RISK", item.getOverallDecision());
    }

    @Test
    @DisplayName("getHistorySummaries should mark overall decision as REVIEW if titles have REVIEW but no HIGH RISK")
    void testGetHistorySummariesWithReview() {
        PublicationApplication app = new PublicationApplication("Magazine", "English", "Maharashtra", "Mumbai", "Weekly");
        app.setId(3L);
        app.setCreatedAt(LocalDateTime.of(2026, 9, 14, 14, 0));

        ProposedTitle title1 = new ProposedTitle("Bombay Weekly", 1);
        app.addProposedTitle(title1);

        when(applicationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(app));

        TitleVerificationResultDto res = new TitleVerificationResultDto();
        res.setProposedTitle("Bombay Weekly");
        res.setFinalDecision("REVIEW");

        when(verificationService.verifyProposedTitle(eq("Bombay Weekly"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(res);

        List<VerificationHistoryItemDto> result = service.getHistorySummaries();

        assertEquals(1, result.size());
        assertEquals("REVIEW", result.get(0).getOverallDecision());
    }

    @Test
    @DisplayName("getComparisonDetails: ACCEPT beats REVIEW regardless of risk score")
    void testComparison_AcceptBeatsReview() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(10L);
        app.addProposedTitle(new ProposedTitle("Option One", 1));
        app.addProposedTitle(new ProposedTitle("Option Two", 2));

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Option One");
        res1.setFinalDecision("REVIEW");
        res1.setRiskScore(10.0);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Option Two");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(35.0);

        when(verificationService.verifyProposedTitle(eq("Option One"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Option Two"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(10L);

        assertNotNull(comparison);
        assertEquals("Option Two", comparison.getRecommendedTitle().getProposedTitle());
        assertEquals("ACCEPT", comparison.getRecommendedTitle().getFinalDecision());
        assertTrue(comparison.getRecommendationExplanation().contains("Option #2"));
        assertTrue(comparison.getRecommendationExplanation().contains("Option Two"));
    }

    @Test
    @DisplayName("getComparisonDetails: REVIEW beats HIGH RISK regardless of risk score")
    void testComparison_ReviewBeatsHighRisk() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(11L);
        app.addProposedTitle(new ProposedTitle("High Risk Option", 1));
        app.addProposedTitle(new ProposedTitle("Review Option", 2));

        when(applicationRepository.findById(11L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("High Risk Option");
        res1.setFinalDecision("HIGH RISK");
        res1.setRiskScore(20.0);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Review Option");
        res2.setFinalDecision("REVIEW");
        res2.setRiskScore(50.0);

        when(verificationService.verifyProposedTitle(eq("High Risk Option"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Review Option"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(11L);

        assertNotNull(comparison);
        assertEquals("Review Option", comparison.getRecommendedTitle().getProposedTitle());
        assertEquals("REVIEW", comparison.getRecommendedTitle().getFinalDecision());
    }

    @Test
    @DisplayName("getComparisonDetails: lower risk score wins within the same decision tier")
    void testComparison_LowerRiskWinsWithinSameDecision() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(12L);
        app.addProposedTitle(new ProposedTitle("Higher Risk Accept", 1));
        app.addProposedTitle(new ProposedTitle("Lower Risk Accept", 2));

        when(applicationRepository.findById(12L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Higher Risk Accept");
        res1.setFinalDecision("ACCEPT");
        res1.setRiskScore(25.0);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Lower Risk Accept");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(12.0);

        when(verificationService.verifyProposedTitle(eq("Higher Risk Accept"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Lower Risk Accept"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(12L);

        assertNotNull(comparison);
        assertEquals("Lower Risk Accept", comparison.getRecommendedTitle().getProposedTitle());
        assertEquals(12.0, comparison.getRecommendedTitle().getRiskScore());
    }

    @Test
    @DisplayName("getComparisonDetails: fewer triggered rules acts as tie-breaker")
    void testComparison_RuleCountTieBreaker() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(13L);
        app.addProposedTitle(new ProposedTitle("Option With Rule", 1));
        app.addProposedTitle(new ProposedTitle("Option Clean", 2));

        when(applicationRepository.findById(13L)).thenReturn(Optional.of(app));

        RuleResultDto triggeredRule = new RuleResultDto("RULE_1", "Rule One", RuleSeverity.HIGH, true, "Triggered", "word", "fix");
        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Option With Rule");
        res1.setFinalDecision("ACCEPT");
        res1.setRiskScore(15.0);
        res1.setRuleResults(List.of(triggeredRule));

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Option Clean");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(15.0);
        res2.setRuleResults(Collections.emptyList());

        when(verificationService.verifyProposedTitle(eq("Option With Rule"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Option Clean"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(13L);

        assertNotNull(comparison);
        assertEquals("Option Clean", comparison.getRecommendedTitle().getProposedTitle());
    }

    @Test
    @DisplayName("getComparisonDetails: lower maximum collision similarity acts as tie-breaker")
    void testComparison_SimilarityTieBreaker() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(14L);
        app.addProposedTitle(new ProposedTitle("Higher Similarity Title", 1));
        app.addProposedTitle(new ProposedTitle("Lower Similarity Title", 2));

        when(applicationRepository.findById(14L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Higher Similarity Title");
        res1.setFinalDecision("ACCEPT");
        res1.setRiskScore(15.0);
        res1.setRuleResults(Collections.emptyList());
        res1.setHighestFuzzySimilarity(0.70);
        res1.setHighestBm25Similarity(0.20);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Lower Similarity Title");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(15.0);
        res2.setRuleResults(Collections.emptyList());
        res2.setHighestFuzzySimilarity(0.25);
        res2.setHighestBm25Similarity(0.35);

        when(verificationService.verifyProposedTitle(eq("Higher Similarity Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Lower Similarity Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(14L);

        assertNotNull(comparison);
        assertEquals("Lower Similarity Title", comparison.getRecommendedTitle().getProposedTitle());
    }

    @Test
    @DisplayName("getComparisonDetails: title preference order breaks tie when all signals are identical")
    void testComparison_TitleOrderFinalTieBreaker() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(15L);
        app.addProposedTitle(new ProposedTitle("Preferred Option One", 1));
        app.addProposedTitle(new ProposedTitle("Tied Option Two", 2));

        when(applicationRepository.findById(15L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("Preferred Option One");
        res1.setFinalDecision("ACCEPT");
        res1.setRiskScore(15.0);
        res1.setRuleResults(Collections.emptyList());
        res1.setHighestFuzzySimilarity(0.30);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Tied Option Two");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(15.0);
        res2.setRuleResults(Collections.emptyList());
        res2.setHighestFuzzySimilarity(0.30);

        when(verificationService.verifyProposedTitle(eq("Preferred Option One"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Tied Option Two"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);

        TitleComparisonDto comparison = service.getComparisonDetails(15L);

        assertNotNull(comparison);
        assertEquals("Preferred Option One", comparison.getRecommendedTitle().getProposedTitle());
    }

    @Test
    @DisplayName("getComparisonDetails: correctly handles single-title application")
    void testComparison_SingleTitleApplication() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(16L);
        app.addProposedTitle(new ProposedTitle("Solo Title", 1));

        when(applicationRepository.findById(16L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res = new TitleVerificationResultDto();
        res.setProposedTitle("Solo Title");
        res.setFinalDecision("ACCEPT");
        res.setRiskScore(10.0);

        when(verificationService.verifyProposedTitle(eq("Solo Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res);

        TitleComparisonDto comparison = service.getComparisonDetails(16L);

        assertNotNull(comparison);
        assertEquals(1, comparison.getVerificationResults().size());
        assertEquals("Solo Title", comparison.getRecommendedTitle().getProposedTitle());
        assertTrue(comparison.getRecommendationExplanation().contains("sole submitted title"));
    }

    @Test
    @DisplayName("getComparisonDetails: correctly handles empty proposed titles list")
    void testComparison_EmptyResultHandling() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(17L);

        when(applicationRepository.findById(17L)).thenReturn(Optional.of(app));

        TitleComparisonDto comparison = service.getComparisonDetails(17L);

        assertNotNull(comparison);
        assertTrue(comparison.getVerificationResults().isEmpty());
        assertNull(comparison.getRecommendedTitle());
        assertEquals("No title verification results available for comparison.", comparison.getRecommendationExplanation());
    }

    @Test
    @DisplayName("getComparisonDetails: preserves original applicant preference order in verificationResults")
    void testComparison_PreservesOriginalApplicantPreferenceOrder() {
        PublicationApplication app = new PublicationApplication("Newspaper", "English", "Delhi", "Central", "Daily");
        app.setId(18L);
        app.addProposedTitle(new ProposedTitle("First Title", 1));
        app.addProposedTitle(new ProposedTitle("Second Title", 2));
        app.addProposedTitle(new ProposedTitle("Third Title", 3));

        when(applicationRepository.findById(18L)).thenReturn(Optional.of(app));

        TitleVerificationResultDto res1 = new TitleVerificationResultDto();
        res1.setProposedTitle("First Title");
        res1.setFinalDecision("REVIEW");
        res1.setRiskScore(30.0);

        TitleVerificationResultDto res2 = new TitleVerificationResultDto();
        res2.setProposedTitle("Second Title");
        res2.setFinalDecision("ACCEPT");
        res2.setRiskScore(10.0);

        TitleVerificationResultDto res3 = new TitleVerificationResultDto();
        res3.setProposedTitle("Third Title");
        res3.setFinalDecision("HIGH RISK");
        res3.setRiskScore(75.0);

        when(verificationService.verifyProposedTitle(eq("First Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res1);
        when(verificationService.verifyProposedTitle(eq("Second Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res2);
        when(verificationService.verifyProposedTitle(eq("Third Title"), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(res3);

        TitleComparisonDto comparison = service.getComparisonDetails(18L);

        assertNotNull(comparison);
        assertEquals(3, comparison.getVerificationResults().size());
        assertEquals("First Title", comparison.getVerificationResults().get(0).getProposedTitle());
        assertEquals("Second Title", comparison.getVerificationResults().get(1).getProposedTitle());
        assertEquals("Third Title", comparison.getVerificationResults().get(2).getProposedTitle());

        // But recommended is Option 2 (Second Title)
        assertEquals("Second Title", comparison.getRecommendedTitle().getProposedTitle());
    }
}

