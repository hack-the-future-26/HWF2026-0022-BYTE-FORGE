package com.titleverify.titleverify_ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiEmbeddingServiceTest {

    private TitleNormalizationService normalizationService;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private GeminiEmbeddingService service;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        service = new GeminiEmbeddingService(normalizationService, restClient, "gemini-embedding-001", "MOCK_API_KEY");
    }

    @Test
    @DisplayName("Should successfully parse real dynamic embedding vector from Gemini REST API response")
    void testSuccessfulEmbeddingParsing() {
        String jsonResponse = """
                {
                  "embedding": {
                    "values": [0.123, -0.456, 0.789, 0.012]
                  }
                }
                """;

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "MOCK_API_KEY"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<float[]> result = service.getEmbedding("Daily Evening News");

        assertTrue(result.isPresent());
        float[] vector = result.get();
        assertEquals(4, vector.length);
        assertEquals(0.123f, vector[0], 0.0001f);
        assertEquals(-0.456f, vector[1], 0.0001f);
        assertEquals(0.789f, vector[2], 0.0001f);
        assertEquals(0.012f, vector[3], 0.0001f);

        mockServer.verify();
    }

    @Test
    @DisplayName("Should return embedding from in-memory cache on duplicate normalized title requests")
    void testCachingBehavior() {
        String jsonResponse = """
                {
                  "embedding": {
                    "values": [0.5, 0.5, 0.5]
                  }
                }
                """;

        // Expect ONLY 1 HTTP call
        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<float[]> firstCall = service.getEmbedding("Daily Evening News");
        Optional<float[]> secondCall = service.getEmbedding("  daily  evening   news ");

        assertTrue(firstCall.isPresent());
        assertTrue(secondCall.isPresent());
        assertArrayEquals(firstCall.get(), secondCall.get());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should return Optional.empty() when API key is missing or blank")
    void testMissingApiKey() {
        if (System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isBlank()) {
            return;
        }
        GeminiEmbeddingService unconfiguredService = new GeminiEmbeddingService(
                normalizationService, RestClient.create(), "gemini-embedding-001", ""
        );

        assertFalse(unconfiguredService.isAvailable());
        Optional<float[]> result = unconfiguredService.getEmbedding("Daily Evening News");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle malformed JSON response safely by returning Optional.empty()")
    void testMalformedResponse() {
        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent"))
                .andRespond(withSuccess("{ \"unexpected\": \"format\" }", MediaType.APPLICATION_JSON));

        Optional<float[]> result = service.getEmbedding("Daily Evening News");
        assertTrue(result.isEmpty());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle empty embedding values array by returning Optional.empty()")
    void testEmptyEmbeddingValues() {
        String jsonResponse = """
                {
                  "embedding": {
                    "values": []
                  }
                }
                """;

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<float[]> result = service.getEmbedding("Daily Evening News");
        assertTrue(result.isEmpty());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle HTTP error status codes (e.g. 500, 429) safely by returning Optional.empty()")
    void testApiErrorResponse() {
        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent"))
                .andRespond(withServerError());

        Optional<float[]> result = service.getEmbedding("Daily Evening News");
        assertTrue(result.isEmpty());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle null or blank title inputs safely")
    void testNullOrBlankInput() {
        assertTrue(service.getEmbedding(null).isEmpty());
        assertTrue(service.getEmbedding("").isEmpty());
        assertTrue(service.getEmbedding("   ").isEmpty());
    }
}
