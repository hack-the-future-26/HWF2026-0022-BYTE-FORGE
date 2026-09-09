package com.titleverify.titleverify_ai.config;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistryDataSeederTest {

    private RegisteredPublicationTitleRepository repository;
    private TitleNormalizationService normalizationService;
    private RegistryDataSeeder seeder;
    private List<RegisteredPublicationTitle> databaseStore;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RegisteredPublicationTitleRepository.class);
        normalizationService = new TitleNormalizationService();
        seeder = new RegistryDataSeeder(repository, normalizationService);
        databaseStore = new ArrayList<>();

        // In-memory mock behavior for findByNormalizedTitle and save
        when(repository.findByNormalizedTitle(anyString())).thenAnswer(invocation -> {
            String norm = invocation.getArgument(0);
            return databaseStore.stream()
                    .filter(item -> item.getNormalizedTitle().equalsIgnoreCase(norm))
                    .findFirst();
        });

        when(repository.save(any(RegisteredPublicationTitle.class))).thenAnswer(invocation -> {
            RegisteredPublicationTitle entity = invocation.getArgument(0);
            databaseStore.add(entity);
            return entity;
        });
    }

    @Test
    @DisplayName("Should seed demo data on first startup with status DEMO_DATA")
    void testSeedDataLoads() {
        seeder.run();

        assertFalse(databaseStore.isEmpty(), "Demo seed items should be saved to repository.");
        assertTrue(databaseStore.size() >= 15, "Expected at least 15 demo titles to be seeded.");
        
        // Verify all seeded records are marked DEMO_DATA
        for (RegisteredPublicationTitle title : databaseStore) {
            assertEquals("DEMO_DATA", title.getStatus(), "Seeded publication title status must be DEMO_DATA");
            assertNotNull(title.getNormalizedTitle(), "Normalized title must not be null");
        }

        // Verify specific required titles are present
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Namaskar News")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Mysuru Chronicle")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("India Samachar")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Colour News")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Daily Evening News")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("India News")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Hindu")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("Indian Express")));
        assertTrue(databaseStore.stream().anyMatch(t -> t.getTitle().equals("ನಮಸ್ಕಾರ ಹಿಂದೂ")));
    }

    @Test
    @DisplayName("Should be idempotent: duplicate seed execution should not create duplicates")
    void testDuplicateSeedExecutionDoesNotCreateDuplicates() {
        seeder.run();
        int initialSize = databaseStore.size();

        // Run seeder second time
        seeder.run();
        int secondSize = databaseStore.size();

        assertEquals(initialSize, secondSize, "Re-running seeder should not duplicate demo records.");
    }

    @Test
    @DisplayName("Should retrieve seeded demo titles via normalized title lookup")
    void testDemoTitlesCanBeRetrieval() {
        seeder.run();

        Optional<RegisteredPublicationTitle> retrievedOpt = repository.findByNormalizedTitle("namaskar news");
        assertTrue(retrievedOpt.isPresent(), "Namaskar News should be retrievable by normalized title.");
        assertEquals("Namaskar News", retrievedOpt.get().getTitle());
        assertEquals("DEMO_DATA", retrievedOpt.get().getStatus());

        Optional<RegisteredPublicationTitle> kannadaOpt = repository.findByNormalizedTitle("ನಮಸ್ಕಾರ ಹಿಂದೂ");
        assertTrue(kannadaOpt.isPresent(), "Kannada title 'ನಮಸ್ಕಾರ ಹಿಂದೂ' should be retrievable by normalized title.");
        assertEquals("ನಮಸ್ಕಾರ ಹಿಂದೂ", kannadaOpt.get().getTitle());
        assertEquals("ನಮಸ್ಕಾರ ಹಿಂದೂ", kannadaOpt.get().getNormalizedTitle());
    }
}
