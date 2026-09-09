package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.RuleResultDto;
import com.titleverify.titleverify_ai.dto.RuleSeverity;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DetailedAnalysisControllerTest {

    private MockMvc mockMvc;
    private PublicationApplicationService applicationService;
    private AnalyzerController analyzerController;

    @BeforeEach
    void setUp() {
        applicationService = Mockito.mock(PublicationApplicationService.class);
        analyzerController = new AnalyzerController(applicationService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(analyzerController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET /analysis/{id} should return 200 OK and render detailed_analysis view with real backend model values")
    void testShowDetailedAnalysisPageSuccess() throws Exception {
        Long appId = 100L;

        RuleResultDto rule = new RuleResultDto("PROHIBITED_WORD", "Prohibited Word Rule", RuleSeverity.HIGH, true, "Prohibited word 'crime' detected", "crime", "Recommendation text");
        TitleVerificationResultDto titleResult = new TitleVerificationResultDto(
                "Crime Today", "crime today", false, null, List.of("India News"),
                "CANDIDATE_SIMILARITY_ANALYSIS", List.of(), 0.2, "India News", 0.2, "India News", 0.2, "India News",
                List.of(rule), "HIGH RISK", 85.0, "HIGH RISK", 15.0, "India News",
                List.of("Rule Triggered [Prohibited Word Rule]: Prohibited word 'crime' detected"),
                "Choose a substantially different core title before official submission."
        );

        ApplicationAnalysisDetailsDto details = new ApplicationAnalysisDetailsDto(
                appId, "Newspaper", "English", "Delhi", "Central", "Daily",
                LocalDateTime.now(), List.of(titleResult), 1, 0, 0, 1
        );

        when(applicationService.getApplicationDetails(appId)).thenReturn(details);

        mockMvc.perform(get("/analysis/" + appId))
                .andExpect(status().isOk())
                .andExpect(view().name("detailed_analysis"))
                .andExpect(model().attributeExists("analysisDetails"));
    }

    @Test
    @DisplayName("GET /analysis/{id} with invalid application ID should return 404 NOT FOUND")
    void testShowDetailedAnalysisPageNotFound() throws Exception {
        Long invalidId = 99999L;
        when(applicationService.getApplicationDetails(invalidId))
                .thenThrow(new IllegalArgumentException("Application not found with ID: " + invalidId));

        mockMvc.perform(get("/analysis/" + invalidId))
                .andExpect(status().isNotFound());
    }
}
