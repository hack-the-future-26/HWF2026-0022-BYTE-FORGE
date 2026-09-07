package com.titleverify.titleverify_ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "titleverify.embedding.provider", havingValue = "development")
public class DevelopmentEmbeddingService implements EmbeddingService {

    private static final int VECTOR_DIMENSION = 64;
    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();

    @Value("${titleverify.embedding.provider:development}")
    private String provider;

    @Value("${titleverify.embedding.api-key:}")
    private String apiKey;

    private final TitleNormalizationService normalizationService;

    public DevelopmentEmbeddingService(TitleNormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }

    @Override
    public Optional<float[]> getEmbedding(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String normalized = normalizationService.normalize(text);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        // Return from cache if present
        if (embeddingCache.containsKey(normalized)) {
            return Optional.of(embeddingCache.get(normalized));
        }

        float[] vector = generateDeterministicEmbedding(normalized);
        embeddingCache.put(normalized, vector);
        return Optional.of(vector);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private float[] generateDeterministicEmbedding(String normalizedText) {
        float[] vector = new float[VECTOR_DIMENSION];
        String[] words = normalizedText.split("\\s+");

        for (String word : words) {
            String conceptKey = mapConceptAlias(word);

            int wordHash = Math.abs(conceptKey.hashCode());
            int primaryIdx = wordHash % VECTOR_DIMENSION;
            vector[primaryIdx] += 3.0f;

            // Character 2-grams
            for (int i = 0; i < conceptKey.length() - 1; i++) {
                String bigram = conceptKey.substring(i, i + 2);
                int bgHash = Math.abs(bigram.hashCode());
                int bgIdx = bgHash % VECTOR_DIMENSION;
                vector[bgIdx] += 0.5f;
            }
        }

        // Normalize vector to unit length (||v|| = 1.0)
        double sumSq = 0.0;
        for (float val : vector) {
            sumSq += val * val;
        }

        if (sumSq > 0.0) {
            float norm = (float) Math.sqrt(sumSq);
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    private String mapConceptAlias(String word) {
        switch (word.toLowerCase(Locale.ROOT)) {
            case "bulletin":
            case "journal":
            case "gazette":
            case "paper":
            case "express":
                return "news";
            case "eve":
            case "night":
                return "evening";
            case "morn":
                return "morning";
            default:
                return word;
        }
    }
}
