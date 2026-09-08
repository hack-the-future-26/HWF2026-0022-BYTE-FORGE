package com.titleverify.titleverify_ai.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class FuzzySimilarityService {

    // Development thresholds (configurable)
    public static final double HIGH_SIMILARITY_THRESHOLD = 0.85;
    public static final double MODERATE_SIMILARITY_THRESHOLD = 0.65;

    /**
     * Calculates Levenshtein similarity between two titles.
     * Formula: similarity = 1.0 - (levenshteinDistance / max(lengthA, lengthB))
     * Returns a score between 0.0 (completely distinct) and 1.0 (identical).
     */
    public double calculateSimilarity(String titleA, String titleB) {
        if (titleA == null || titleB == null) {
            return 0.0;
        }

        String strA = titleA.trim().toLowerCase(Locale.ROOT);
        String strB = titleB.trim().toLowerCase(Locale.ROOT);

        if (strA.isEmpty() && strB.isEmpty()) {
            return 1.0;
        }
        if (strA.isEmpty() || strB.isEmpty()) {
            return 0.0;
        }
        if (strA.equals(strB)) {
            return 1.0;
        }

        int maxLength = Math.max(strA.length(), strB.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = calculateLevenshteinDistance(strA, strB);
        double similarity = 1.0 - ((double) distance / maxLength);

        // Clamp value between 0.0 and 1.0
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    /**
     * Calculates the Levenshtein Distance (edit distance) between two strings.
     */
    public int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return (s1 == null && s2 == null) ? 0 : (s1 == null ? s2.length() : s1.length());
        }
        if (s1.equals(s2)) {
            return 0;
        }
        if (s1.isEmpty()) {
            return s2.length();
        }
        if (s2.isEmpty()) {
            return s1.length();
        }

        int[] dp = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            int previousDiagonal = dp[0];
            dp[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int temp = dp[j];
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), previousDiagonal + cost);
                previousDiagonal = temp;
            }
        }
        return dp[s2.length()];
    }

    public String classifySimilarityLevel(double similarityScore) {
        if (similarityScore >= HIGH_SIMILARITY_THRESHOLD) {
            return "HIGH";
        } else if (similarityScore >= MODERATE_SIMILARITY_THRESHOLD) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
}
