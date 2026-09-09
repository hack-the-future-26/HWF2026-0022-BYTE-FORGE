package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeminiHeadlineGenerationServiceTest {

    private GeminiHeadlineGenerationService headlineGenerationService;

    @BeforeEach
    void setUp() {
        headlineGenerationService = new GeminiHeadlineGenerationService(
                RestClient.create(),
                "gemini-3.6-flash",
                "" // Empty string simulates @Value("${GEMINI_API_KEY:}") when unset in application.properties
        );
    }

    @Test
    void testApiKeyResolutionWhenValueIsEmptyString() {
        // When @Value injection gives "", constructor resolves via System.getenv
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            assertTrue(headlineGenerationService.isAvailable(), "Should resolve API key from environment variable when @Value is empty.");
        } else {
            assertFalse(headlineGenerationService.isAvailable(), "Should be unavailable when no environment key is present.");
        }
    }

    @Test
    void testApiKeyMissingReturnsFalse() {
        GeminiHeadlineGenerationService serviceWithoutKey = new GeminiHeadlineGenerationService(
                RestClient.create(),
                "gemini-3.6-flash",
                null
        );
        // If system env also empty, isAvailable is false
        if (System.getenv("GEMINI_API_KEY") == null || System.getenv("GEMINI_API_KEY").isBlank()) {
            assertFalse(serviceWithoutKey.isAvailable());
        }
    }

    @Test
    void testCorrectModelAndEndpointConfigured() {
        assertEquals("gemini-3.6-flash", headlineGenerationService.getModel());
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent", headlineGenerationService.getEndpoint());
        assertTrue(headlineGenerationService.getEndpoint().endsWith(":generateContent"), "Must call :generateContent endpoint, not :embedContent");
    }

    @Test
    void testCorrectJsonRequestFormatStructure() {
        Map<String, Object> payload = headlineGenerationService.createRequestPayload("Sample Prompt Text");
        assertNotNull(payload);
        assertTrue(payload.containsKey("contents"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contents = (List<Map<String, Object>>) payload.get("contents");
        assertFalse(contents.isEmpty());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contents.get(0).get("parts");
        assertEquals("Sample Prompt Text", parts.get(0).get("text"));
    }

    @Test
    void testCorrectResponseParsingFromGeminiCandidatesStructure() {
        Map<String, Object> sampleGeminiResponse = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "[\"Headline 1\", \"Headline 2\", \"Headline 3\", \"Headline 4\", \"Headline 5\"]")
                                )
                        ))
                )
        );

        String extractedText = headlineGenerationService.extractTextFromGeminiResponse(sampleGeminiResponse);
        assertNotNull(extractedText);

        List<String> headlines = headlineGenerationService.parseHeadlinesFromJson(extractedText);
        assertEquals(5, headlines.size());
        assertEquals("Headline 1", headlines.get(0));
        assertEquals("Headline 5", headlines.get(4));
    }

    @Test
    void testGenerateHeadlinesFallbackModeReturnsFiveHeadlines() {
        String article = "Karnataka government announces new digital media policies and publication registration guidelines for news organizations across the state.";
        
        // Force offline fallback by testing fallback method directly or when API key unavailable
        List<String> headlines = headlineGenerationService.generateFallbackHeadlines(article);

        assertNotNull(headlines);
        assertEquals(5, headlines.size());
        assertTrue(headlines.get(0).toLowerCase().contains("karnataka"));
    }

    @Test
    void testInputTooShortThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> headlineGenerationService.generateHeadlines("Too short"));
    }

    @Test
    void testInputTooLongThrowsException() {
        String longContent = "A".repeat(5001);
        assertThrows(IllegalArgumentException.class, () -> headlineGenerationService.generateHeadlines(longContent));
    }

    @Test
    void testParseHeadlinesFromJsonWithMarkdownCodeBlock() {
        String json = """
                ```json
                [
                  "Title 1",
                  "Title 2",
                  "Title 3",
                  "Title 4",
                  "Title 5"
                ]
                ```
                """;
        List<String> parsed = headlineGenerationService.parseHeadlinesFromJson(json);
        assertEquals(5, parsed.size());
        assertEquals("Title 1", parsed.get(0));
    }

    @Test
    void testParseHeadlinesFromMalformedJsonFallbackRegex() {
        String text = "Here are the top headlines:\n1. \"City Expands Metro Rail Network\"\n2. \"New Tech Hub Opened in Bengaluru\"\n3. \"Monsoon Rains Bring Relief\"\n4. \"State Government Approves Budget\"\n5. \"Sports Complex Inaugurated Today\"";
        List<String> parsed = headlineGenerationService.parseHeadlinesFromJson(text);
        assertEquals(5, parsed.size());
        assertEquals("City Expands Metro Rail Network", parsed.get(0));
    }
}
