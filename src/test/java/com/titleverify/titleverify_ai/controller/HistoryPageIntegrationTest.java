package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
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
                .andExpect(content().string(containsString("/analysis/42")));
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
                .andExpect(content().string(containsString("Verification History")));
    }
}
