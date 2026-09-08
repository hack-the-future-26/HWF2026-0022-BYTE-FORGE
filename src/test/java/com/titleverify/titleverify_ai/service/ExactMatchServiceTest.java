package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ExactMatchServiceTest {

    private RegisteredPublicationTitleRepository repository;
    private ExactMatchService exactMatchService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RegisteredPublicationTitleRepository.class);
        exactMatchService = new ExactMatchService(repository);
    }

    @Test
    @DisplayName("Should find exact match when normalized title exists in repository")
    void testFindExactMatch_ExactTitleFound() {
        RegisteredPublicationTitle title = new RegisteredPublicationTitle(
                "India News", "india news", "English", "Delhi", "Daily", "Newspaper", "DEMO_DATA"
        );

        when(repository.findByNormalizedTitle("india news")).thenReturn(Optional.of(title));

        Optional<RegisteredPublicationTitle> result = exactMatchService.findExactMatch("india news");

        assertTrue(result.isPresent());
        assertEquals("India News", result.get().getTitle());
    }

    @Test
    @DisplayName("Should return empty optional when title is not found in repository")
    void testFindExactMatch_TitleNotFound() {
        when(repository.findByNormalizedTitle(anyString())).thenReturn(Optional.empty());

        Optional<RegisteredPublicationTitle> result = exactMatchService.findExactMatch("unknown title");

        assertFalse(result.isPresent());
    }
}
