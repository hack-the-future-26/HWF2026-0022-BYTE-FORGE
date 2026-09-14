package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.RegistryImportResultDto;
import com.titleverify.titleverify_ai.service.RegistryImportService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegistryImportControllerTest {

    private MockMvc mockMvc;
    private RegistryImportService registryImportService;
    private RegistryImportController controller;

    @BeforeEach
    void setUp() {
        registryImportService = Mockito.mock(RegistryImportService.class);
        controller = new RegistryImportController(registryImportService);

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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    // ==========================================
    // GET /registry/import Tests (Step 4)
    // ==========================================

    @Test
    @DisplayName("GET /registry/import returns HTTP 200 and renders registry_import template")
    void testShowImportPageReturns200AndRendersTemplate() throws Exception {
        mockMvc.perform(get("/registry/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("registry_import"));
    }

    @Test
    @DisplayName("GET /registry/import contains page title and explanation")
    void testImportPageTitleAndExplanation() throws Exception {
        mockMvc.perform(get("/registry/import"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registered Title Import")))
                .andExpect(content().string(containsString("Import registered publication titles from a CSV file into the TitleVerify registry.")));
    }

    @Test
    @DisplayName("GET /registry/import contains file upload control and Import Titles button")
    void testImportPageContainsUploadControlAndButton() throws Exception {
        mockMvc.perform(get("/registry/import"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"file\"")))
                .andExpect(content().string(containsString("type=\"file\"")))
                .andExpect(content().string(containsString("Import Titles")));
    }

    @Test
    @DisplayName("GET /registry/import contains CSV format schema and example")
    void testImportPageContainsCsvFormatInformation() throws Exception {
        mockMvc.perform(get("/registry/import"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Title,Language,State,Periodicity,Publication Type")))
                .andExpect(content().string(containsString("Karnataka Morning,English,Karnataka,Daily,Newspaper")));
    }

    @Test
    @DisplayName("GET /registry/import contains IMPORTED status and DEMO_DATA preservation explanation")
    void testImportPageContainsImportedAndDemoDataExplanation() throws Exception {
        mockMvc.perform(get("/registry/import"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("IMPORTED")))
                .andExpect(content().string(containsString("DEMO_DATA")));
    }

    // ==========================================
    // POST /registry/import Tests (Step 3 preserved)
    // ==========================================

    @Test
    @DisplayName("Valid CSV upload returns HTTP 200 OK with import counts")
    void testValidCsvUploadReturns200() throws Exception {
        String csvContent = "Title,Language,State,Periodicity,Publication Type\nBangalore Mirror,English,Karnataka,Daily,Newspaper";
        MockMultipartFile file = new MockMultipartFile("file", "titles.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        RegistryImportResultDto resultDto = new RegistryImportResultDto(1, 1, 0, 0, 0, Collections.emptyList());
        when(registryImportService.importCsv(any(MultipartFile.class))).thenReturn(resultDto);

        mockMvc.perform(multipart("/registry/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.insertedCount").value(1))
                .andExpect(jsonPath("$.duplicateCount").value(0))
                .andExpect(jsonPath("$.invalidCount").value(0))
                .andExpect(jsonPath("$.errorCount").value(0));

        verify(registryImportService, times(1)).importCsv(any(MultipartFile.class));
    }

    @Test
    @DisplayName("Multipart file is properly passed to RegistryImportService")
    void testMultipartFileIsPassedToService() throws Exception {
        String csvContent = "Title,Language\nMorning Star,English";
        MockMultipartFile file = new MockMultipartFile("file", "titles.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        RegistryImportResultDto resultDto = new RegistryImportResultDto(1, 1, 0, 0, 0, Collections.emptyList());
        when(registryImportService.importCsv(any(MultipartFile.class))).thenReturn(resultDto);

        mockMvc.perform(multipart("/registry/import").file(file))
                .andExpect(status().isOk());

        verify(registryImportService).importCsv(argThat(f -> f != null && "titles.csv".equals(f.getOriginalFilename())));
    }

    @Test
    @DisplayName("Response JSON contains complete import result statistics")
    void testResponseContainsImportResultCounts() throws Exception {
        String csvContent = "Title\nTest Title";
        MockMultipartFile file = new MockMultipartFile("file", "titles.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        RegistryImportResultDto resultDto = new RegistryImportResultDto(10, 7, 2, 1, 0, List.of("Skipped 2 duplicates"));
        when(registryImportService.importCsv(any(MultipartFile.class))).thenReturn(resultDto);

        mockMvc.perform(multipart("/registry/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(10))
                .andExpect(jsonPath("$.insertedCount").value(7))
                .andExpect(jsonPath("$.duplicateCount").value(2))
                .andExpect(jsonPath("$.invalidCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(0))
                .andExpect(jsonPath("$.errorDetails[0]").value("Skipped 2 duplicates"));
    }

    @Test
    @DisplayName("Empty file returns HTTP 400 Bad Request")
    void testEmptyFileReturns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/registry/import").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetails[0]").value("Uploaded file is missing or empty"));

        verify(registryImportService, never()).importCsv(any());
    }

    @Test
    @DisplayName("Missing file parameter returns HTTP 400 Bad Request")
    void testMissingFileReturns400() throws Exception {
        mockMvc.perform(multipart("/registry/import"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetails[0]").value("Uploaded file is missing or empty"));

        verify(registryImportService, never()).importCsv(any());
    }

    @Test
    @DisplayName("Unsupported file type (e.g. PDF) returns HTTP 400 Bad Request")
    void testUnsupportedPdfUploadReturns400() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf",
                "%PDF-1.4 Fake PDF Content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/registry/import").file(pdfFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetails[0]").value("Unsupported file type or invalid upload. Please upload a valid CSV file."));

        verify(registryImportService, never()).importCsv(any());
    }

    @Test
    @DisplayName("Unsupported binary file returns HTTP 400 Bad Request")
    void testBinaryFileReturns400() throws Exception {
        MockMultipartFile binaryFile = new MockMultipartFile("file", "data.bin", "application/octet-stream",
                new byte[]{0x00, 0x01, 0x02, 0x03, 0x04});

        mockMvc.perform(multipart("/registry/import").file(binaryFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetails[0]").value("Unsupported file type or invalid upload. Please upload a valid CSV file."));

        verify(registryImportService, never()).importCsv(any());
    }

    @Test
    @DisplayName("Service failure returns HTTP 500 Internal Server Error")
    void testServiceFailureReturns500() throws Exception {
        String csvContent = "Title\nTest Title";
        MockMultipartFile file = new MockMultipartFile("file", "titles.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(registryImportService.importCsv(any(MultipartFile.class))).thenThrow(new RuntimeException("Database timeout error"));

        mockMvc.perform(multipart("/registry/import").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorDetails[0]", Matchers.containsString("Database timeout error")));
    }
}
