package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RegistryImportResultDto;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistryImportServiceTest {

    private RegisteredPublicationTitleRepository repository;
    private TitleNormalizationService normalizationService;
    private Bm25SimilarityService bm25SimilarityService;
    private RegistryImportService service;

    @BeforeEach
    void setUp() {
        repository = mock(RegisteredPublicationTitleRepository.class);
        normalizationService = new TitleNormalizationService();
        bm25SimilarityService = mock(Bm25SimilarityService.class);
        service = new RegistryImportService(repository, normalizationService, bm25SimilarityService);
    }

    private MockMultipartFile createCsvFile(String csvContent) {
        return new MockMultipartFile("file", "titles.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Successful CSV import with multiple titles")
    void testSuccessfulCsvImport() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Bangalore Mirror,English,Karnataka,Daily,Newspaper
                Deccan Herald,English,Karnataka,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(2, result.getInsertedCount());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(0, result.getInvalidCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.getErrorDetails().isEmpty());

        verify(repository, times(1)).saveAll(any());
        verify(bm25SimilarityService, times(1)).refreshCorpusStats();
    }

    @Test
    @DisplayName("Import titles with optional fields present, partially present, and absent")
    void testMultipleTitlesWithOptionalFields() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Full Spec Title,English,Karnataka,Daily,Newspaper
                Partial Title,Hindi,,,
                Minimal Title
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RegisteredPublicationTitle>> captor = ArgumentCaptor.forClass(List.class);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(3, result.getTotalProcessed());
        assertEquals(3, result.getInsertedCount());

        verify(repository).saveAll(captor.capture());
        List<RegisteredPublicationTitle> saved = captor.getValue();
        assertEquals(3, saved.size());

        // Full spec
        assertEquals("Full Spec Title", saved.get(0).getTitle());
        assertEquals("English", saved.get(0).getLanguage());
        assertEquals("Karnataka", saved.get(0).getState());
        assertEquals("Daily", saved.get(0).getPeriodicity());
        assertEquals("Newspaper", saved.get(0).getPublicationType());

        // Partial
        assertEquals("Partial Title", saved.get(1).getTitle());
        assertEquals("Hindi", saved.get(1).getLanguage());
        assertNull(saved.get(1).getState());
        assertNull(saved.get(1).getPeriodicity());
        assertNull(saved.get(1).getPublicationType());

        // Minimal
        assertEquals("Minimal Title", saved.get(2).getTitle());
        assertNull(saved.get(2).getLanguage());
        assertNull(saved.get(2).getState());
        assertNull(saved.get(2).getPeriodicity());
        assertNull(saved.get(2).getPublicationType());
    }

    @Test
    @DisplayName("Duplicate titles inside the same CSV batch are detected and skipped")
    void testDuplicateTitlesInsideSameCsv() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                National Herald,English,Delhi,Daily,Newspaper
                National Herald,English,Delhi,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getInsertedCount());
        assertEquals(1, result.getDuplicateCount());
        assertEquals(0, result.getInvalidCount());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    @DisplayName("Duplicate title already existing in database is skipped")
    void testDuplicateTitleAlreadyExistingInDatabase() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Namaskar News,Hindi,Uttar Pradesh,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle("namaskar news")).thenReturn(true);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(1, result.getTotalProcessed());
        assertEquals(0, result.getInsertedCount());
        assertEquals(1, result.getDuplicateCount());
        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Case and spacing variations normalize to duplicate")
    void testCaseAndSpacingDuplicatesAfterNormalization() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Deccan Herald,English,Karnataka,Daily,Newspaper
                  DECCAN   HERALD  ,English,Karnataka,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getInsertedCount());
        assertEquals(1, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Rows with blank or missing title are counted as invalid")
    void testBlankTitleRows() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                ,English,Delhi,Daily,Newspaper
                Valid Chronicle,English,Goa,Monthly,Magazine
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getInsertedCount());
        assertEquals(1, result.getInvalidCount());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    @DisplayName("Malformed rows with unclosed quotes do not crash the entire import")
    void testMalformedRows() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                "Unclosed Quote Publication,English,Delhi,Daily,Newspaper
                Valid Post,English,Maharashtra,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getInsertedCount());
        assertEquals(1, result.getErrorCount());
        assertFalse(result.getErrorDetails().isEmpty());
    }

    @Test
    @DisplayName("Empty CSV file returns 0 counts safely")
    void testEmptyCsv() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        RegistryImportResultDto result = service.importCsv(emptyFile);

        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getInsertedCount());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(0, result.getInvalidCount());
        assertEquals(0, result.getErrorCount());
        assertFalse(result.getErrorDetails().isEmpty());

        verify(repository, never()).saveAll(any());
        verify(bm25SimilarityService, never()).refreshCorpusStats();
    }

    @Test
    @DisplayName("Null file returns safe result without exception")
    void testNullFile() {
        RegistryImportResultDto result = service.importCsv(null);

        assertNotNull(result);
        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getInsertedCount());
        assertFalse(result.getErrorDetails().isEmpty());

        verify(repository, never()).saveAll(any());
        verify(bm25SimilarityService, never()).refreshCorpusStats();
    }

    @Test
    @DisplayName("Imported records use status = IMPORTED")
    void testImportedRecordsUseImportedStatus() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Kannada Prabha,Kannada,Karnataka,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RegisteredPublicationTitle>> captor = ArgumentCaptor.forClass(List.class);

        service.importCsv(createCsvFile(csv));

        verify(repository).saveAll(captor.capture());
        List<RegisteredPublicationTitle> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("IMPORTED", saved.get(0).getStatus());
    }

    @Test
    @DisplayName("Existing DEMO_DATA records are not overwritten")
    void testExistingDemoDataRecordsNotOverwritten() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Colour News,English,Delhi,Daily,Newspaper
                """;

        // "Colour News" is existing demo data in the database
        when(repository.existsByNormalizedTitle("colour news")).thenReturn(true);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(1, result.getTotalProcessed());
        assertEquals(0, result.getInsertedCount());
        assertEquals(1, result.getDuplicateCount());

        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("BM25 corpus refresh happens exactly once after successful import")
    void testBm25CorpusRefreshHappensOnceAfterSuccessfulImport() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Title One,English,Delhi,Daily,Newspaper
                Title Two,English,Delhi,Daily,Newspaper
                Title Three,English,Delhi,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        service.importCsv(createCsvFile(csv));

        verify(bm25SimilarityService, times(1)).refreshCorpusStats();
    }

    @Test
    @DisplayName("Import result counts are mathematically consistent")
    void testImportResultCountsAreConsistent() {
        String csv = """
                Title,Language,State,Periodicity,Publication Type
                Unique Title,English,Karnataka,Daily,Newspaper
                Unique Title,English,Karnataka,Daily,Newspaper
                Namaskar News,Hindi,UP,Daily,Newspaper
                ,English,Delhi,Daily,Newspaper
                "Malformed Quote Row,English,Delhi,Daily,Newspaper
                """;

        when(repository.existsByNormalizedTitle("namaskar news")).thenReturn(true);
        when(repository.existsByNormalizedTitle("unique title")).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(csv));

        assertEquals(5, result.getTotalProcessed());
        assertEquals(1, result.getInsertedCount());
        assertEquals(2, result.getDuplicateCount()); // 1 CSV duplicate, 1 DB duplicate
        assertEquals(1, result.getInvalidCount());   // 1 blank title
        assertEquals(1, result.getErrorCount());     // 1 malformed quote row

        assertEquals(result.getTotalProcessed(),
                result.getInsertedCount() + result.getDuplicateCount() + result.getInvalidCount() + result.getErrorCount());
    }

    @Test
    @DisplayName("Batch saving is triggered when records exceed batch size")
    void testBatchSavingWhenExceedingBatchThreshold() {
        StringBuilder sb = new StringBuilder("Title,Language,State,Periodicity,Publication Type\n");
        for (int i = 1; i <= 505; i++) {
            sb.append("Unique Title ").append(i).append(",English,State,Daily,Newspaper\n");
        }

        when(repository.existsByNormalizedTitle(anyString())).thenReturn(false);

        RegistryImportResultDto result = service.importCsv(createCsvFile(sb.toString()));

        assertEquals(505, result.getTotalProcessed());
        assertEquals(505, result.getInsertedCount());

        // First batch of 500, then second batch of 5
        verify(repository, times(2)).saveAll(any());
        verify(bm25SimilarityService, times(1)).refreshCorpusStats();
    }
}
