package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class Bm25SimilarityServiceTest {

    private TitleNormalizationService normalizationService;
    private Bm25SimilarityService bm25Service;

    @BeforeEach
    void setUp() {
        normalizationService = new TitleNormalizationService();
        bm25Service = new Bm25SimilarityService(normalizationService);
    }

    @Test
    @DisplayName("Identical titles should yield a normalized BM25 score of 1.0")
    void testExactMatchSelfScoreIsOne() {
        double score = bm25Service.calculateBm25Similarity("India Today", "India Today");
        assertEquals(1.0, score, 0.0001);
    }

    @Test
    @DisplayName("Permuted word orders of identical words should yield 1.0")
    void testPermutedWordOrderYieldsOne() {
        double score = bm25Service.calculateBm25Similarity("Karnataka Herald", "Herald Karnataka");
        assertEquals(1.0, score, 0.0001);
    }

    @Test
    @DisplayName("Completely orthogonal titles with zero word overlap should return 0.0")
    void testZeroWordOverlapReturnsZero() {
        double score = bm25Service.calculateBm25Similarity("Bengaluru Tech Review", "Namaskar Hindi Samachar");
        assertEquals(0.0, score, 0.0001);
    }

    @Test
    @DisplayName("Null and empty inputs should safely return 0.0 without throwing exceptions")
    void testNullAndEmptyInputsReturnZero() {
        assertEquals(0.0, bm25Service.calculateBm25Similarity(null, "India Today"));
        assertEquals(0.0, bm25Service.calculateBm25Similarity("India Today", null));
        assertEquals(0.0, bm25Service.calculateBm25Similarity("", "India Today"));
        assertEquals(0.0, bm25Service.calculateBm25Similarity("   ", "India Today"));
        assertEquals(0.0, bm25Service.calculateBm25Similarity("!@#$%", "India Today"));
    }

    @Test
    @DisplayName("Rare/distinctive terms should be weighted higher than common ubiquitous terms (IDF property)")
    void testRareTermWeightedHigherThanCommonTerm() {
        double scoreRareMatch = bm25Service.calculateBm25Similarity("Mysuru News", "Mysuru Herald");
        double scoreCommonMatch = bm25Service.calculateBm25Similarity("Mysuru News", "Delhi News");

        assertTrue(scoreRareMatch > scoreCommonMatch,
                "Match on distinctive term 'Mysuru' (" + scoreRareMatch + ") should exceed match on common term 'News' (" + scoreCommonMatch + ")");
    }

    @Test
    @DisplayName("Should classify BM25 similarity into HIGH, MODERATE, or LOW levels")
    void testClassifyBm25Level() {
        assertEquals("HIGH", bm25Service.classifyBm25Level(1.0));
        assertEquals("HIGH", bm25Service.classifyBm25Level(0.75));
        assertEquals("MODERATE", bm25Service.classifyBm25Level(0.74));
        assertEquals("MODERATE", bm25Service.classifyBm25Level(0.50));
        assertEquals("LOW", bm25Service.classifyBm25Level(0.49));
        assertEquals("LOW", bm25Service.classifyBm25Level(0.0));
    }

    @Test
    @DisplayName("Should rank candidates in descending order of BM25 lexical similarity")
    void testRankCandidates() {
        RegisteredPublicationTitle c1 = new RegisteredPublicationTitle("Delhi Daily", "delhi daily", "English", "Delhi", "Daily", "Newspaper", "DEMO_DATA");
        RegisteredPublicationTitle c2 = new RegisteredPublicationTitle("Mysuru Chronicle", "mysuru chronicle", "English", "Karnataka", "Daily", "Newspaper", "DEMO_DATA");
        RegisteredPublicationTitle c3 = new RegisteredPublicationTitle("Kerala Agriculture Journal", "kerala agriculture journal", "English", "Kerala", "Monthly", "Journal", "DEMO_DATA");

        List<RegisteredPublicationTitle> candidates = List.of(c1, c2, c3);
        List<RegisteredPublicationTitle> ranked = bm25Service.rankCandidates("Mysuru Daily News", candidates);

        assertNotNull(ranked);
        assertEquals(3, ranked.size());
        assertEquals("Mysuru Chronicle", ranked.get(0).getTitle());
        assertEquals("Delhi Daily", ranked.get(1).getTitle());
        assertEquals("Kerala Agriculture Journal", ranked.get(2).getTitle());
    }

    @Test
    @DisplayName("Should update corpus statistics from RegisteredPublicationTitleRepository")
    void testRefreshCorpusStatsFromRepository() {
        RegisteredPublicationTitleRepository mockRepo = Mockito.mock(RegisteredPublicationTitleRepository.class);
        RegisteredPublicationTitle t1 = new RegisteredPublicationTitle("Bengaluru Times", "bengaluru times", "English", "Karnataka", "Daily", "Newspaper", "DEMO_DATA");
        RegisteredPublicationTitle t2 = new RegisteredPublicationTitle("Bengaluru Herald", "bengaluru herald", "English", "Karnataka", "Daily", "Newspaper", "DEMO_DATA");
        RegisteredPublicationTitle t3 = new RegisteredPublicationTitle("Bengaluru Mirror", "bengaluru mirror", "English", "Karnataka", "Daily", "Newspaper", "DEMO_DATA");

        when(mockRepo.findAll()).thenReturn(List.of(t1, t2, t3));

        Bm25SimilarityService repoBm25 = new Bm25SimilarityService(normalizationService, mockRepo);
        repoBm25.refreshCorpusStats();

        double idfBengaluru = repoBm25.calculateIdf("bengaluru");
        double idfRare = repoBm25.calculateIdf("unseenword");

        assertTrue(idfRare > idfBengaluru, "Unseen rare word should have higher IDF than corpus term");
    }

    @Test
    @DisplayName("Raw BM25 computation handles edge cases cleanly")
    void testComputeRawBm25EdgeCases() {
        assertEquals(0.0, bm25Service.computeRawBm25(null, List.of("test")));
        assertEquals(0.0, bm25Service.computeRawBm25(List.of("test"), null));
        assertEquals(0.0, bm25Service.computeRawBm25(List.of(), List.of("test")));
        assertEquals(0.0, bm25Service.computeRawBm25(List.of("test"), List.of()));
    }
}
