package com.titleverify.titleverify_ai.service;

import java.util.Optional;

public interface EmbeddingService {

    Optional<float[]> getEmbedding(String text);

    boolean isAvailable();
}
