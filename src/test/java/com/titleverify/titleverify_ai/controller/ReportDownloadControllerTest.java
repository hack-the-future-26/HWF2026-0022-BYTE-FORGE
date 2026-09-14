package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import com.titleverify.titleverify_ai.service.VerificationReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportDownloadControllerTest {

    private MockMvc mockMvc;
    private PublicationApplicationService applicationService;
    private VerificationReportService reportService;
    private AnalyzerController analyzerController;

    @BeforeEach
    void setUp() {
        applicationService = Mockito.mock(PublicationApplicationService.class);
        reportService = Mockito.mock(VerificationReportService.class);
        analyzerController = new AnalyzerController(applicationService, reportService);

        mockMvc = MockMvcBuilders.standaloneSetup(analyzerController).build();
    }

    @Test
    @DisplayName("GET /report/{id}/download with valid application ID should return HTTP 200 OK")
    void testDownloadPdfReportStatus200() throws Exception {
        Long appId = 42L;
        TitleComparisonDto comparisonDto = new TitleComparisonDto(
                appId, "Newspaper", "English", "Delhi", "Central", "Daily",
                LocalDateTime.now(), Collections.emptyList(), null, null
        );

        byte[] fakePdf = "%PDF-1.4 Sample PDF content".getBytes(StandardCharsets.US_ASCII);

        when(applicationService.getComparisonDetails(appId)).thenReturn(comparisonDto);
        when(reportService.generatePdfReport(comparisonDto)).thenReturn(fakePdf);

        mockMvc.perform(get("/report/" + appId + "/download"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /report/{id}/download response Content-Type must be application/pdf")
    void testDownloadPdfReportContentType() throws Exception {
        Long appId = 42L;
        TitleComparisonDto comparisonDto = new TitleComparisonDto(
                appId, "Newspaper", "English", "Delhi", "Central", "Daily",
                LocalDateTime.now(), Collections.emptyList(), null, null
        );

        byte[] fakePdf = "%PDF-1.4 Sample PDF content".getBytes(StandardCharsets.US_ASCII);

        when(applicationService.getComparisonDetails(appId)).thenReturn(comparisonDto);
        when(reportService.generatePdfReport(comparisonDto)).thenReturn(fakePdf);

        mockMvc.perform(get("/report/" + appId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("GET /report/{id}/download Content-Disposition must contain expected filename")
    void testDownloadPdfReportContentDisposition() throws Exception {
        Long appId = 42L;
        TitleComparisonDto comparisonDto = new TitleComparisonDto(
                appId, "Newspaper", "English", "Delhi", "Central", "Daily",
                LocalDateTime.now(), Collections.emptyList(), null, null
        );

        byte[] fakePdf = "%PDF-1.4 Sample PDF content".getBytes(StandardCharsets.US_ASCII);

        when(applicationService.getComparisonDetails(appId)).thenReturn(comparisonDto);
        when(reportService.generatePdfReport(comparisonDto)).thenReturn(fakePdf);

        mockMvc.perform(get("/report/" + appId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"TitleVerify_Report_APP-42.pdf\""));
    }

    @Test
    @DisplayName("GET /report/{id}/download response body should contain PDF bytes starting with %PDF-")
    void testDownloadPdfResponseBodyHeader() throws Exception {
        Long appId = 88L;
        TitleVerificationResultDto titleResult = new TitleVerificationResultDto(
                "Deccan Chronicle", "deccan chronicle", false, null, List.of(),
                "COMPLETED", List.of(), 0.1, null, 0.1, null, 0.1, null,
                List.of(), "ACCEPT", 10.0, "LOW", 90.0, null,
                List.of(), "Approved"
        );

        TitleComparisonDto comparisonDto = new TitleComparisonDto(
                appId, "Newspaper", "English", "Maharashtra", "Mumbai", "Daily",
                LocalDateTime.now(), List.of(titleResult), titleResult, "Recommendation text"
        );

        VerificationReportService realReportService = new VerificationReportService();
        AnalyzerController realController = new AnalyzerController(applicationService, realReportService);
        MockMvc realMockMvc = MockMvcBuilders.standaloneSetup(realController).build();

        when(applicationService.getComparisonDetails(appId)).thenReturn(comparisonDto);

        byte[] responseBytes = realMockMvc.perform(get("/report/" + appId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"TitleVerify_Report_APP-88.pdf\""))
                .andReturn().getResponse().getContentAsByteArray();

        assertNotNull(responseBytes);
        assertTrue(responseBytes.length > 0);
        String header = new String(responseBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("GET /report/{id}/download with invalid/non-existing ID should return 404 NOT FOUND")
    void testDownloadPdfReportNotFound() throws Exception {
        Long invalidId = 99999L;
        when(applicationService.getComparisonDetails(invalidId))
                .thenThrow(new IllegalArgumentException("Application not found with ID: " + invalidId));

        mockMvc.perform(get("/report/" + invalidId + "/download"))
                .andExpect(status().isNotFound());
    }
}
