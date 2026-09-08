package com.titleverify.titleverify_ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "titleverify.embedding.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiEmbeddingService.class);
    private static final String DEFAULT_GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    private final TitleNormalizationService normalizationService;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();

    @Autowired
    public GeminiEmbeddingService(TitleNormalizationService normalizationService,
                                  @Value("${titleverify.embedding.model:gemini-embedding-001}") String model,
                                  @Value("${GEMINI_API_KEY:}") String apiKey) {
        this(normalizationService, RestClient.create(), model, apiKey);
    }

    public GeminiEmbeddingService(TitleNormalizationService normalizationService,
                                  RestClient restClient,
                                  String model,
                                  String apiKey) {
        this.normalizationService = normalizationService;
        this.restClient = restClient;
        this.model = model;
        String resolvedKey = (apiKey != null && !apiKey.isBlank()) ? apiKey : System.getenv("GEMINI_API_KEY");
        this.apiKey = (resolvedKey != null) ? resolvedKey.trim() : "";
    }

    @Override
    public Optional<float[]> getEmbedding(String text) {
        if (!isAvailable()) {
            logger.warn("GeminiEmbeddingService requested, but GEMINI_API_KEY is not configured.");
            return Optional.empty();
        }

        if (text == null) {
            return Optional.empty();
        }

        String normalized = normalizationService.normalize(text);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        if (embeddingCache.containsKey(normalized)) {
            return Optional.of(embeddingCache.get(normalized));
        }

        try {
            GeminiEmbeddingRequest requestBody = createRequestPayload(text);

            GeminiEmbeddingResponse response = restClient.post()
                    .uri(DEFAULT_GEMINI_ENDPOINT)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiEmbeddingResponse.class);

            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                logger.error("Gemini API returned an empty or malformed response.");
                return Optional.empty();
            }

            List<Float> values = response.embedding().values();
            if (values.isEmpty()) {
                logger.error("Gemini API returned an empty vector values array.");
                return Optional.empty();
            }

            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i);
            }

            embeddingCache.put(normalized, vector);
            return Optional.of(vector);
        } catch (Exception e) {
            logger.error("Error invoking Gemini embedding API: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private GeminiEmbeddingRequest createRequestPayload(String text) {
        String fullModelName = model.startsWith("models/") ? model : "models/" + model;
        return new GeminiEmbeddingRequest(
                fullModelName,
                new GeminiContent(List.of(new GeminiPart(text))),
                "SEMANTIC_SIMILARITY"
        );
    }

    // DTO records for Gemini REST API payload
    public record GeminiEmbeddingRequest(String model, GeminiContent content, String taskType) {}
    public record GeminiContent(List<GeminiPart> parts) {}
    public record GeminiPart(String text) {}

    public record GeminiEmbeddingResponse(GeminiEmbeddingData embedding) {}
    public record GeminiEmbeddingData(List<Float> values) {}
}
