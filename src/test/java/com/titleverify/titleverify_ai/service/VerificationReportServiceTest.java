package com.titleverify.titleverify_ai.service;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerificationReportServiceTest {

    private VerificationReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new VerificationReportService();
    }

    private String extractAllText(byte[] pdfBytes) throws IOException {
        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            sb.append(extractor.getTextFromPage(i)).append(" ");
        }
        reader.close();
        return sb.toString().replaceAll("\\s+", " ");
    }

    @Test
    @DisplayName("Should generate valid PDF bytes starting with %PDF- header")
    void testGeneratePdfReportValidHeader() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(1);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);

        PdfReader reader = new PdfReader(pdfBytes);
        assertTrue(reader.getNumberOfPages() >= 1);
        reader.close();
    }

    @Test
    @DisplayName("Should include application metadata in generated PDF")
    void testApplicationMetadataAppearsInPdf() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(1);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);
        String text = extractAllText(pdfBytes);

        assertTrue(text.contains("TV-APP-101"));
        assertTrue(text.contains("#101"));
        assertTrue(text.contains("Newspaper"));
        assertTrue(text.contains("English"));
        assertTrue(text.contains("Maharashtra"));
        assertTrue(text.contains("Mumbai"));
        assertTrue(text.contains("Daily"));
    }

    @Test
    @DisplayName("Should include proposed title data and multi-signal metrics")
    void testProposedTitleDataAppearsInPdf() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(2);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);
        String text = extractAllText(pdfBytes);

        assertTrue(text.contains("Mumbai Chronicle"));
        assertTrue(text.contains("Bombay Herald"));
        assertTrue(text.contains("ACCEPT"));
        assertTrue(text.contains("REVIEW"));
        assertTrue(text.contains("Herald News"));
    }

    @Test
    @DisplayName("Should include recommendation choice and explanation")
    void testRecommendationDataAppearsInPdf() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(2);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);
        String text = extractAllText(pdfBytes);

        assertTrue(text.contains("Recommended Choice"));
        assertTrue(text.contains("Option #1"));
        assertTrue(text.contains("Option #1 (Mumbai Chronicle) has the lowest risk score"));
    }

    @Test
    @DisplayName("Should handle multiple proposed titles (up to 5 titles)")
    void testHandleMultipleTitlesUpToFive() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(5);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);
        assertNotNull(pdfBytes);

        String text = extractAllText(pdfBytes);
        for (int i = 1; i <= 5; i++) {
            assertTrue(text.contains("Option #" + i));
        }
    }

    @Test
    @DisplayName("Should handle empty title list safely without throwing exception")
    void testHandleEmptyTitleList() throws IOException {
        TitleComparisonDto emptyDto = new TitleComparisonDto(
                202L, "Magazine", "Hindi", "Delhi", "Central", "Monthly",
                LocalDateTime.now(), Collections.emptyList(), null, null
        );

        byte[] pdfBytes = reportService.generatePdfReport(emptyDto);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String text = extractAllText(pdfBytes);
        assertTrue(text.contains("TV-APP-202"));
        assertTrue(text.contains("No titles available for comparison"));
    }

    @Test
    @DisplayName("Should handle null comparison details safely")
    void testHandleNullComparisonDetails() throws IOException {
        byte[] pdfBytes = reportService.generatePdfReport(null);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String text = extractAllText(pdfBytes);
        assertTrue(text.contains("TV-APP-N/A"));
        assertTrue(text.contains("TitleVerify AI"));
    }

    @Test
    @DisplayName("Should handle null field values across results safely and display N/A")
    void testHandleNullValuesInResultsSafely() throws IOException {
        TitleVerificationResultDto nullHeavyResult = new TitleVerificationResultDto();
        nullHeavyResult.setProposedTitle("Sparse Title");
        nullHeavyResult.setFinalDecision("ACCEPT");

        TitleComparisonDto nullHeavyDto = new TitleComparisonDto(
                303L, null, null, null, null, null,
                null, List.of(nullHeavyResult), nullHeavyResult, null
        );

        byte[] pdfBytes = reportService.generatePdfReport(nullHeavyDto);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String text = extractAllText(pdfBytes);
        assertTrue(text.contains("Sparse Title"));
        assertTrue(text.contains("N/A"));
    }

    @Test
    @DisplayName("Should include the exact regulatory prototype disclaimer text")
    void testDisclaimerIsIncluded() throws IOException {
        TitleComparisonDto comparisonDto = createSampleDto(1);

        byte[] pdfBytes = reportService.generatePdfReport(comparisonDto);
        String text = extractAllText(pdfBytes);

        String expectedDisclaimer = "DISCLAIMER: This report is generated by TitleVerify AI as an automated pre-submission screening prototype. " +
                "Risk scores, confidence values, and similarity signals are advisory and do not represent official PRGI approval decisions or legal clearance.";
        assertTrue(text.contains(expectedDisclaimer));
    }

    private TitleComparisonDto createSampleDto(int count) {
        List<TitleVerificationResultDto> results = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            RuleResultDto rule1 = new RuleResultDto(
                    "PROHIBITED_WORD", "Prohibited Word Rule", RuleSeverity.HIGH,
                    i == 2, i == 2 ? "Triggered prohibited word check" : "Passed",
                    i == 2 ? "evidence" : null, "Recommendation"
            );

            TitleVerificationResultDto res = new TitleVerificationResultDto(
                    i == 1 ? "Mumbai Chronicle" : (i == 2 ? "Bombay Herald" : "Title Option " + i),
                    "normalized", false, null, List.of("Herald News"),
                    "COMPLETED", List.of(), 0.10 * i, "Herald News", 0.08 * i, "Herald News", 0.15 * i, "Herald News",
                    List.of(rule1), i == 1 ? "ACCEPT" : "REVIEW", 10.0 * i, i == 1 ? "LOW" : "MEDIUM", 90.0 - (i * 5),
                    "Herald News", List.of("Title inspection complete"), "Proceed with verification."
            );
            res.setHighestBm25Similarity(0.5 * i);
            res.setTopBm25Match("Herald News");

            results.add(res);
        }

        TitleVerificationResultDto rec = results.get(0);
        return new TitleComparisonDto(
                101L, "Newspaper", "English", "Maharashtra", "Mumbai", "Daily",
                LocalDateTime.of(2026, 9, 14, 15, 30),
                results, rec,
                "Option #1 (Mumbai Chronicle) has the lowest risk score and highest clearance confidence."
        );
    }
}
