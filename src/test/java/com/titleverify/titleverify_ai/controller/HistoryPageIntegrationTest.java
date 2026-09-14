package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.dto.VerificationHistoryItemDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistoryPageIntegrationTest {

    private MockMvc mockMvc;
    private PublicationApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = Mockito.mock(PublicationApplicationService.class);
        AnalyzerController analyzerController = new AnalyzerController(applicationService);

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setCharacterEncoding("UTF-8");

        mockMvc = MockMvcBuilders.standaloneSetup(analyzerController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET /analyze should render analyzer template successfully with history navigation link")
    void testRenderAnalyzerPage() throws Exception {
        mockMvc.perform(get("/analyze"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Application Analyzer")))
                .andExpect(content().string(containsString("Verification History")));
    }

    @Test
    @DisplayName("GET /history with empty applications should render clean empty state")
    void testRenderHistoryEmptyState() throws Exception {
        when(applicationService.getHistorySummaries()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Title Verification History")))
                .andExpect(content().string(containsString("No Verification History Yet")))
                .andExpect(content().string(containsString("Start New Analysis")));
    }

    @Test
    @DisplayName("GET /history with applications should render application cards, status badges, and view analysis link")
    void testRenderHistoryWithItems() throws Exception {
        VerificationHistoryItemDto item1 = new VerificationHistoryItemDto(
                42L,
                LocalDateTime.of(2026, 9, 14, 15, 30),
                "Newspaper",
                "English",
                "Maharashtra",
                "Mumbai",
                "Daily",
                2,
                List.of("Mumbai Herald", "Bombay Mirror"),
                "ACCEPT"
        );

        when(applicationService.getHistorySummaries()).thenReturn(List.of(item1));

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Application #42")))
                .andExpect(content().string(containsString("ACCEPT")))
                .andExpect(content().string(containsString("Mumbai Herald")))
                .andExpect(content().string(containsString("Bombay Mirror")))
                .andExpect(content().string(containsString("/analysis/42")))
                .andExpect(content().string(containsString("/comparison/42")));
    }

    @Test
    @DisplayName("GET /analysis/{id} should render detailed analysis template with navigation")
    void testRenderDetailedAnalysisPage() throws Exception {
        RuleResultDto rule = new RuleResultDto("PROHIBITED_WORD", "Prohibited Word Rule", RuleSeverity.HIGH, false, "Passed", null, "Passed");
        TitleVerificationResultDto titleResult = new TitleVerificationResultDto(
                "Mumbai Herald", "mumbai herald", false, null, List.of("Herald News"),
                "CANDIDATE_SIMILARITY_ANALYSIS", List.of(), 0.1, "Herald News", 0.1, "Herald News", 0.1, "Herald News",
                List.of(rule), "ACCEPT", 12.0, "ACCEPT", 88.0, "Herald News",
                List.of("Title passed checks."),
                "Proceed to submission."
        );

        ApplicationAnalysisDetailsDto details = new ApplicationAnalysisDetailsDto(
                42L, "Newspaper", "English", "Maharashtra", "Mumbai", "Daily",
                LocalDateTime.of(2026, 9, 14, 15, 30),
                List.of(titleResult), 1, 1, 0, 0
        );

        when(applicationService.getApplicationDetails(42L)).thenReturn(details);

        mockMvc.perform(get("/analysis/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Detailed Verification Report")))
                .andExpect(content().string(containsString("Mumbai Herald")))
                .andExpect(content().string(containsString("Verification History")))
                .andExpect(content().string(containsString("/comparison/42")));
    }

    @Test
    @DisplayName("GET /comparison/{id} should render 5-title comparison matrix template with recommended choice and comparison table")
    void testRenderComparisonPageWithItems() throws Exception {
        RuleResultDto rule = new RuleResultDto("PROHIBITED_WORD", "Prohibited Word Rule", RuleSeverity.HIGH, false, "Passed", null, "Passed");
        TitleVerificationResultDto titleResult1 = new TitleVerificationResultDto(
                "Mumbai Herald", "mumbai herald", false, null, List.of("Herald News"),
                "CANDIDATE_SIMILARITY_ANALYSIS", List.of(), 0.15, "Herald News", 0.12, "Herald News", 0.25, "Herald News",
                List.of(rule), "ACCEPT", 12.0, "LOW", 88.0, "Herald News",
                List.of("Title passed checks."),
                "Proceed to submission."
        );
        titleResult1.setHighestBm25Similarity(0.45);
        titleResult1.setTopBm25Match("Herald News");

        TitleVerificationResultDto titleResult2 = new TitleVerificationResultDto(
                "Bombay Mirror", "bombay mirror", false, null, List.of("Mirror Today"),
                "CANDIDATE_SIMILARITY_ANALYSIS", List.of(), 0.45, "Mirror Today", 0.50, "Mirror Today", 0.60, "Mirror Today",
                List.of(rule), "REVIEW", 48.0, "MEDIUM", 52.0, "Mirror Today",
                List.of("Moderate similarity with existing title."),
                "Review potential title conflicts."
        );
        titleResult2.setHighestBm25Similarity(1.80);
        titleResult2.setTopBm25Match("Mirror Today");

        TitleComparisonDto comparisonDto = new TitleComparisonDto(
                42L, "Newspaper", "English", "Maharashtra", "Mumbai", "Daily",
                LocalDateTime.of(2026, 9, 14, 15, 30),
                List.of(titleResult1, titleResult2),
                titleResult1,
                "Option #1 (Mumbai Herald) is the safest choice with lowest risk score (12.0) and ACCEPT decision."
        );

        when(applicationService.getComparisonDetails(42L)).thenReturn(comparisonDto);

        mockMvc.perform(get("/comparison/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Title Comparison Matrix")))
                .andExpect(content().string(containsString("5-Title Side-by-Side Comparison")))
                .andExpect(content().string(containsString("RECOMMENDED CHOICE")))
                .andExpect(content().string(containsString("Mumbai Herald")))
                .andExpect(content().string(containsString("Bombay Mirror")))
                .andExpect(content().string(containsString("Option #1")))
                .andExpect(content().string(containsString("Option #2")))
                .andExpect(content().string(containsString("/analysis/42")));
    }

    @Test
    @DisplayName("GET /comparison/{id} with empty results should render clean empty state message")
    void testRenderComparisonPageEmptyState() throws Exception {
        TitleComparisonDto emptyDto = new TitleComparisonDto(
                99L, "Newspaper", "English", "Delhi", "Central", "Daily",
                LocalDateTime.of(2026, 9, 14, 15, 30),
                Collections.emptyList(),
                null,
                null
        );

        when(applicationService.getComparisonDetails(99L)).thenReturn(emptyDto);

        mockMvc.perform(get("/comparison/99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No titles available for comparison")))
                .andExpect(content().string(containsString("Start New Analysis")));
    }
}
