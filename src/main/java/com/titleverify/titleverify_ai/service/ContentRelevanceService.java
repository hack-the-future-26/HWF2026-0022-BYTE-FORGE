package com.titleverify.titleverify_ai.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentRelevanceService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "from", "up", "about", "into", "through", "after", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will", "would", "shall", "should",
            "may", "might", "must", "can", "could", "this", "that", "these", "those", "it", "its", "news",
            "report", "special", "daily", "weekly", "today", "express", "times", "post");

    public double calculateRelevance(String headline, String articleContent) {
        if (headline == null || headline.isBlank() || articleContent == null || articleContent.isBlank()) {
            return 0.0;
        }

        List<String> headlineTokens = extractKeywords(headline);
        if (headlineTokens.isEmpty()) {
            return 50.0; // Default baseline if headline contains only stopwords
        }

        List<String> articleTokens = extractKeywords(articleContent);
        if (articleTokens.isEmpty()) {
            return 0.0;
        }

        Set<String> articleTokenSet = new HashSet<>(articleTokens);
        Map<String, Integer> articleFreq = new HashMap<>();
        for (String token : articleTokens) {
            articleFreq.put(token, articleFreq.getOrDefault(token, 0) + 1);
        }

        // 1. Direct Keyword Match Ratio (Headline words found in article)
        int matchedWords = 0;
        int weightedScore = 0;
        for (String hToken : headlineTokens) {
            if (articleTokenSet.contains(hToken)) {
                matchedWords++;
                // Give higher weight if word appears multiple times or is prominent in article
                int freq = articleFreq.getOrDefault(hToken, 0);
                weightedScore += (freq > 1 ? 2 : 1);
            }
        }

        double coverageRatio = (double) matchedWords / headlineTokens.size();

        String normHeadline = headline.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").trim();
        String normArticle = articleContent.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").trim();

        double phraseBonus = 0.0;
        if (normArticle.contains(normHeadline)) {
            phraseBonus = 0.3;
        } else {
            // Check for 2-word phrase matches
            String[] words = normHeadline.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                String bigram = words[i] + " " + words[i + 1];
                if (bigram.length() > 5 && normArticle.contains(bigram)) {
                    phraseBonus += 0.1;
                }
            }
        }

        // 3. Combine scores into 0.0 to 100.0%
        double baseScore = (coverageRatio * 70.0) + (Math.min(phraseBonus, 0.3) * 30.0);

        // Cap minimum at 30.0 for generated headlines that represent extracted topics
        // well, max at 98.0
        double finalRelevance = Math.min(98.0, Math.max(35.0, baseScore));
        return Math.round(finalRelevance * 10.0) / 10.0;
    }

    public String classifyRelevanceLevel(double relevanceScore) {
        if (relevanceScore >= 70.0) {
            return "HIGH RELEVANCE";
        } else if (relevanceScore >= 50.0) {
            return "MODERATE RELEVANCE";
        } else {
            return "LOW RELEVANCE";
        }
    }

    private List<String> extractKeywords(String text) {
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ");
        String[] words = cleaned.split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String w : words) {
            if (w.length() > 2 && !STOP_WORDS.contains(w)) {
                keywords.add(w);
            }
        }
        return keywords;
    }
}
