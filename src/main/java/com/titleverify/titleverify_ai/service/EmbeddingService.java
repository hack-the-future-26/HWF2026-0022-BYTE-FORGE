package com.titleverify.titleverify_ai.service;

import java.util.Optional;

public interface EmbeddingService {

    /**
     * Obtains the embedding vector for the given text.
     * Returns Optional.empty() if embedding service or API key is unconfigured or unavailable.
     */
    Optional<float[]> getEmbedding(String text);

    /**
     * Checks if the embedding provider is configured and available.
     */
    boolean isAvailable();
}
