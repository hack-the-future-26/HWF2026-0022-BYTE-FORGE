package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CandidateRetrievalServiceTest {

    private RegisteredPublicationTitleRepository repository;
    private CandidateRetrievalService candidateRetrievalService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RegisteredPublicationTitleRepository.class);
        candidateRetrievalService = new CandidateRetrievalService(repository);
    }

    @Test
    @DisplayName("Should retrieve candidates matching normalized title fragment")
    void testRetrieveCandidates_MatchingCandidateFound() {
        RegisteredPublicationTitle item = new RegisteredPublicationTitle(
                "Mysuru Voice", "mysuru voice", "Kannada", "Karnataka", "Daily", "Newspaper", "DEMO_DATA"
        );

        when(repository.findByNormalizedTitleContaining("mysuru")).thenReturn(List.of(item));

        List<RegisteredPublicationTitle> candidates = candidateRetrievalService.retrieveCandidates("mysuru");

        assertFalse(candidates.isEmpty());
        assertEquals("Mysuru Voice", candidates.get(0).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no candidates match")
    void testRetrieveCandidates_NoCandidateFound() {
        when(repository.findByNormalizedTitleContaining(anyString())).thenReturn(Collections.emptyList());

        List<RegisteredPublicationTitle> candidates = candidateRetrievalService.retrieveCandidates("xyznonexistent");

        assertTrue(candidates.isEmpty());
    }
}
