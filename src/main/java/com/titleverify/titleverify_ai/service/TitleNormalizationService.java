package com.titleverify.titleverify_ai.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class TitleNormalizationService {

    public String normalize(String title) {
        if (title == null) {
            return "";
        }
        // 1. Unicode NFC Normalization
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFC);
        // 2. Convert to lowercase
        normalized = normalized.toLowerCase(Locale.ROOT);
        // 3. Replace non-letter, non-mark, non-number, and non-whitespace characters with space
        normalized = normalized.replaceAll("[^\\p{L}\\p{M}\\p{N}\\s]", " ");
        // 4. Collapse multiple spaces into a single space and trim
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }
}
