package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.HeadlineGenerationRequestDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationResponseDto;
import com.titleverify.titleverify_ai.dto.HeadlineVerificationResultDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import com.titleverify.titleverify_ai.service.HeadlineGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HeadlineGeneratorControllerTest {

    private MockMvc mockMvcPage;
    private MockMvc mockMvcApi;
    private HeadlineGeneratorService headlineGeneratorService;

    @BeforeEach
    void setUp() {
        headlineGeneratorService = Mockito.mock(HeadlineGeneratorService.class);

        HeadlineGeneratorController pageController = new HeadlineGeneratorController();
        HeadlineGeneratorApiController apiController = new HeadlineGeneratorApiController(headlineGeneratorService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvcPage = MockMvcBuilders.standaloneSetup(pageController)
                .setViewResolvers(viewResolver)
                .build();

        mockMvcApi = MockMvcBuilders.standaloneSetup(apiController)
                .build();
    }

    @Test
    void testShowHeadlineGeneratorPageReturns200AndTemplate() throws Exception {
        mockMvcPage.perform(get("/headline-generator"))
                .andExpect(status().isOk())
                .andExpect(view().name("headline_generator"));
    }

    @Test
    void testGenerateHeadlinesApiSuccess() throws Exception {
        TitleVerificationResultDto v = new TitleVerificationResultDto();
        v.setFinalDecision("ACCEPT");
        v.setRiskScore(10.0);

        HeadlineVerificationResultDto item = new HeadlineVerificationResultDto("Karnataka Media News", 85.0, v);
        HeadlineGenerationResponseDto mockResponse = new HeadlineGenerationResponseDto(
                List.of(item),
                item,
                "Strongest overall headline",
                "SUCCESS",
                "Successfully generated and verified headlines."
        );

        when(headlineGeneratorService.generateAndVerifyHeadlines(any())).thenReturn(mockResponse);

        String jsonBody = """
                {
                  "content": "Karnataka state government introduces new publication guidelines for newspapers."
                }
                """;

        mockMvcApi.perform(post("/api/headline-generator/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.recommendedHeadline.generatedHeadline").value("Karnataka Media News"));
    }

    @Test
    void testGenerateHeadlinesApiValidationErrorForShortContent() throws Exception {
        when(headlineGeneratorService.generateAndVerifyHeadlines(any()))
                .thenReturn(new HeadlineGenerationResponseDto(List.of(), null, null, "ERROR", "Content is too short"));

        String jsonBody = """
                {
                  "content": "Short"
                }
                """;

        mockMvcApi.perform(post("/api/headline-generator/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }
}
