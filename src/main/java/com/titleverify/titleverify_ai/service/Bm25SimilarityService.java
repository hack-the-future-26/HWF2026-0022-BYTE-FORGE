package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory BM25 similarity and ranking service for publication titles.
 */
@Service
public class Bm25SimilarityService {

    private static final Logger logger = LoggerFactory.getLogger(Bm25SimilarityService.class);

    public static final double K1 = 1.2;
    public static final double B = 0.75;
    public static final double DEFAULT_AVGDL = 3.0;

    public static final double HIGH_BM25_THRESHOLD = 0.75;
    public static final double MODERATE_BM25_THRESHOLD = 0.50;

    // Baseline frequencies prevent ubiquitous publication words from dominating IDF
    private static final Set<String> COMMON_TERMS = Set.of(
            "news", "daily", "weekly", "times", "the", "a", "an", "post", "today", "now",
            "journal", "bulletin", "gazette", "monthly", "evening", "morning", "press"
    );

    private final TitleNormalizationService normalizationService;
    private final RegisteredPublicationTitleRepository repository;

    private final Map<String, Integer> termDocFrequency = new ConcurrentHashMap<>();
    private int corpusSize = 50;
    private double avgDocLength = DEFAULT_AVGDL;
    private volatile boolean initialized = false;

    public Bm25SimilarityService(TitleNormalizationService normalizationService) {
        this(normalizationService, null);
    }

    @Autowired
    public Bm25SimilarityService(TitleNormalizationService normalizationService,
                                 @Autowired(required = false) RegisteredPublicationTitleRepository repository) {
        this.normalizationService = normalizationService != null ? normalizationService : new TitleNormalizationService();
        this.repository = repository;
        initBaselineTermFrequencies();
    }

    private void initBaselineTermFrequencies() {
        for (String term : COMMON_TERMS) {
            termDocFrequency.put(term, 25);
        }
    }

    public synchronized void refreshCorpusStats() {
        if (repository == null) {
            return;
        }

        try {
            List<RegisteredPublicationTitle> allTitles = repository.findAll();
            if (allTitles.isEmpty()) {
                return;
            }

            termDocFrequency.clear();
            initBaselineTermFrequencies();

            int totalDocs = allTitles.size();
            long totalTokens = 0;

            for (RegisteredPublicationTitle title : allTitles) {
                String raw = title.getNormalizedTitle() != null ? title.getNormalizedTitle() : title.getTitle();
                List<String> tokens = tokenize(raw);
                totalTokens += tokens.size();

                Set<String> uniqueTokens = new HashSet<>(tokens);
                for (String t : uniqueTokens) {
                    termDocFrequency.merge(t, 1, Integer::sum);
                }
            }

            this.corpusSize = Math.max(totalDocs, 50);
            this.avgDocLength = totalTokens > 0 ? (double) totalTokens / totalDocs : DEFAULT_AVGDL;
            this.initialized = true;
            logger.info("BM25 corpus statistics updated: {} documents, avg length: {}", totalDocs, avgDocLength);
        } catch (Exception e) {
            logger.warn("Could not refresh BM25 corpus statistics from database: {}", e.getMessage());
        }
    }

    public double calculateBm25Similarity(String queryTitle, String candidateTitle) {
        if (queryTitle == null || candidateTitle == null) {
            return 0.0;
        }

        List<String> queryTokens = tokenize(queryTitle);
        List<String> candidateTokens = tokenize(candidateTitle);

        if (queryTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0.0;
        }

        if (queryTokens.equals(candidateTokens)) {
            return 1.0;
        }

        ensureInitialized();

        double rawScore = computeRawBm25(queryTokens, candidateTokens);
        if (rawScore <= 0.0) {
            return 0.0;
        }

        // Self-score normalization: score(Q, D) / score(Q, Q) to map into [0.0, 1.0]
        double selfMaxScore = computeRawBm25(queryTokens, queryTokens);
        if (selfMaxScore <= 0.0) {
            return 0.0;
        }

        double normalized = rawScore / selfMaxScore;
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    public double computeRawBm25(List<String> queryTokens, List<String> candidateTokens) {
        if (queryTokens == null || candidateTokens == null || queryTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> candidateTf = new HashMap<>();
        for (String token : candidateTokens) {
            candidateTf.merge(token, 1, Integer::sum);
        }

        int docLen = candidateTokens.size();
        double currentAvgdl = avgDocLength > 0.0 ? avgDocLength : DEFAULT_AVGDL;
        double score = 0.0;

        for (String qTerm : queryTokens) {
            int tf = candidateTf.getOrDefault(qTerm, 0);
            if (tf > 0) {
                double idf = calculateIdf(qTerm);
                double numerator = tf * (K1 + 1.0);
                double denominator = tf + K1 * (1.0 - B + B * (docLen / currentAvgdl));
                score += idf * (numerator / denominator);
            }
        }

        return score;
    }

    public double calculateIdf(String term) {
        int n = termDocFrequency.getOrDefault(term, 1);
        int N = Math.max(corpusSize, 50);

        // Smoothed BM25 IDF: ln(1 + (N - n + 0.5) / (n + 0.5))
        return Math.log(1.0 + ((double) (N - n + 0.5) / (n + 0.5)));
    }

    public List<RegisteredPublicationTitle> rankCandidates(String queryTitle, List<RegisteredPublicationTitle> candidates) {
        if (candidates == null || candidates.isEmpty() || queryTitle == null || queryTitle.trim().isEmpty()) {
            return candidates != null ? new ArrayList<>(candidates) : new ArrayList<>();
        }

        List<RegisteredPublicationTitle> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> {
            String titleA = a.getTitle() != null ? a.getTitle() : "";
            String titleB = b.getTitle() != null ? b.getTitle() : "";
            double scoreA = calculateBm25Similarity(queryTitle, titleA);
            double scoreB = calculateBm25Similarity(queryTitle, titleB);
            return Double.compare(scoreB, scoreA);
        });
        return sorted;
    }

    public String classifyBm25Level(double score) {
        if (score >= HIGH_BM25_THRESHOLD) {
            return "HIGH";
        } else if (score >= MODERATE_BM25_THRESHOLD) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    private List<String> tokenize(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        String normalized = normalizationService.normalize(text);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        String[] words = normalized.split("\\s+");
        List<String> list = new ArrayList<>(words.length);
        for (String w : words) {
            if (!w.isBlank()) {
                list.add(w.trim());
            }
        }
        return list;
    }

    private void ensureInitialized() {
        if (!initialized && repository != null) {
            refreshCorpusStats();
        }
    }
}
