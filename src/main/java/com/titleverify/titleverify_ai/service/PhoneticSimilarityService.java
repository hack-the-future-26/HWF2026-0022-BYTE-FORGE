package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.utility.MetaphoneEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhoneticSimilarityService {

    // Development thresholds (configurable)
    public static final double HIGH_PHONETIC_THRESHOLD = 0.85;
    public static final double MODERATE_PHONETIC_THRESHOLD = 0.65;

    private final TitleNormalizationService normalizationService;
    private final MetaphoneEncoder metaphoneEncoder;
    private final FuzzySimilarityService fuzzySimilarityService;

    public PhoneticSimilarityService(TitleNormalizationService normalizationService,
                                     FuzzySimilarityService fuzzySimilarityService) {
        this.normalizationService = normalizationService;
        this.fuzzySimilarityService = fuzzySimilarityService;
        this.metaphoneEncoder = new MetaphoneEncoder();
    }

    /**
     * Converts a title into a space-separated sequence of word-level Metaphone phonetic codes.
     * Example: "Color News" -> "KLR NS"
     * Example: "Colour News" -> "KLR NS"
     */
    public String encodeTitleToPhonetic(String title) {
        if (title == null) {
            return "";
        }
        String normalized = normalizationService.normalize(title);
        if (normalized.isEmpty()) {
            return "";
        }

        String[] words = normalized.split("\\s+");
        List<String> codes = new ArrayList<>();
        for (String word : words) {
            String code = metaphoneEncoder.encode(word);
            if (!code.isEmpty()) {
                codes.add(code);
            }
        }
        return String.join(" ", codes);
    }

    /**
     * Calculates phonetic similarity between two titles by converting words to phonetic representations
     * and computing string edit distance on the resulting phonetic strings.
     * Returns a score between 0.0 and 1.0.
     */
    public double calculatePhoneticSimilarity(String titleA, String titleB) {
        if (titleA == null || titleB == null) {
            return 0.0;
        }

        String normA = normalizationService.normalize(titleA);
        String normB = normalizationService.normalize(titleB);

        if (normA.isEmpty() && normB.isEmpty()) {
            return 1.0;
        }
        if (normA.isEmpty() || normB.isEmpty()) {
            return 0.0;
        }

        String phoneticA = encodeTitleToPhonetic(titleA);
        String phoneticB = encodeTitleToPhonetic(titleB);

        if (phoneticA.isEmpty() || phoneticB.isEmpty()) {
            return 0.0;
        }
        if (phoneticA.equals(phoneticB)) {
            return 1.0;
        }

        return fuzzySimilarityService.calculateSimilarity(phoneticA, phoneticB);
    }

    /**
     * Categorizes phonetic similarity score into development threshold levels.
     */
    public String classifyPhoneticLevel(double phoneticScore) {
        if (phoneticScore >= HIGH_PHONETIC_THRESHOLD) {
            return "HIGH";
        } else if (phoneticScore >= MODERATE_PHONETIC_THRESHOLD) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
}
