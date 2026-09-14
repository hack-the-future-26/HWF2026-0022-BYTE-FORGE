package com.titleverify.titleverify_ai.service;

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
import java.util.Collections;
import java.util.List;

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
}
