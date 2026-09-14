package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.dto.RegistryImportResultDto;
import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RegistryImportService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryImportService.class);
    private static final int BATCH_SIZE = 500;

    private final RegisteredPublicationTitleRepository registeredTitleRepository;
    private final TitleNormalizationService normalizationService;
    private final Bm25SimilarityService bm25SimilarityService;

    public RegistryImportService(RegisteredPublicationTitleRepository registeredTitleRepository,
                                 TitleNormalizationService normalizationService) {
        this(registeredTitleRepository, normalizationService, null);
    }

    @Autowired
    public RegistryImportService(RegisteredPublicationTitleRepository registeredTitleRepository,
                                 TitleNormalizationService normalizationService,
                                 @Autowired(required = false) Bm25SimilarityService bm25SimilarityService) {
        this.registeredTitleRepository = registeredTitleRepository;
        this.normalizationService = normalizationService;
        this.bm25SimilarityService = bm25SimilarityService;
    }

    public RegistryImportResultDto importCsv(MultipartFile file) {
        RegistryImportResultDto result = new RegistryImportResultDto();

        if (file == null || file.isEmpty()) {
            result.addErrorDetail("Uploaded file is null or empty");
            logger.warn("Registry import aborted: uploaded file is null or empty");
            return result;
        }

        Set<String> seenNormalizedTitles = new HashSet<>();
        List<RegisteredPublicationTitle> batch = new ArrayList<>(BATCH_SIZE);

        int titleCol = 0;
        int langCol = 1;
        int stateCol = 2;
        int periodicityCol = 3;
        int pubTypeCol = 4;

        int rowNum = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;

                // Strip UTF-8 BOM if present on first line
                if (rowNum == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                // Check for empty or blank line
                if (line.trim().isEmpty()) {
                    result.incrementTotalProcessed();
                    result.incrementInvalidCount();
                    result.addErrorDetail("Row " + rowNum + ": Empty or blank line");
                    continue;
                }

                List<String> tokens;
                try {
                    tokens = parseCsvLine(line);
                } catch (Exception e) {
                    result.incrementTotalProcessed();
                    result.incrementErrorCount();
                    result.addErrorDetail("Row " + rowNum + ": Malformed CSV row - " + e.getMessage());
                    continue;
                }

                // Detect header row on the first non-empty line
                if (rowNum == 1 && isHeaderRow(tokens)) {
                    for (int i = 0; i < tokens.size(); i++) {
                        String header = tokens.get(i).trim().toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
                        if (header.equals("title") || header.equals("publicationtitle")) {
                            titleCol = i;
                        } else if (header.equals("language") || header.equals("lang")) {
                            langCol = i;
                        } else if (header.equals("state")) {
                            stateCol = i;
                        } else if (header.equals("periodicity") || header.equals("period")) {
                            periodicityCol = i;
                        } else if (header.equals("publicationtype") || header.equals("type")) {
                            pubTypeCol = i;
                        }
                    }
                    continue;
                }

                String rawTitle = getField(tokens, titleCol);
                if (rawTitle == null || rawTitle.isBlank()) {
                    result.incrementTotalProcessed();
                    result.incrementInvalidCount();
                    result.addErrorDetail("Row " + rowNum + ": Missing mandatory Title");
                    continue;
                }

                String normalizedTitle = normalizationService != null
                        ? normalizationService.normalize(rawTitle)
                        : rawTitle.trim().toLowerCase(Locale.ROOT);

                if (normalizedTitle == null || normalizedTitle.isBlank()) {
                    result.incrementTotalProcessed();
                    result.incrementInvalidCount();
                    result.addErrorDetail("Row " + rowNum + ": Title '" + rawTitle + "' normalized to empty text");
                    continue;
                }

                result.incrementTotalProcessed();

                // Check duplicate within the current CSV batch
                if (seenNormalizedTitles.contains(normalizedTitle)) {
                    result.incrementDuplicateCount();
                    result.addErrorDetail("Row " + rowNum + ": Duplicate title within CSV batch ('" + rawTitle + "')");
                    continue;
                }

                // Check duplicate against existing database records
                if (registeredTitleRepository.existsByNormalizedTitle(normalizedTitle)) {
                    seenNormalizedTitles.add(normalizedTitle);
                    result.incrementDuplicateCount();
                    result.addErrorDetail("Row " + rowNum + ": Title already exists in registry ('" + rawTitle + "')");
                    continue;
                }

                seenNormalizedTitles.add(normalizedTitle);

                String language = getField(tokens, langCol);
                String state = getField(tokens, stateCol);
                String periodicity = getField(tokens, periodicityCol);
                String publicationType = getField(tokens, pubTypeCol);

                RegisteredPublicationTitle entity = new RegisteredPublicationTitle(
                        rawTitle,
                        normalizedTitle,
                        language,
                        state,
                        periodicity,
                        publicationType,
                        "IMPORTED"
                );

                batch.add(entity);

                if (batch.size() >= BATCH_SIZE) {
                    registeredTitleRepository.saveAll(new ArrayList<>(batch));
                    result.setInsertedCount(result.getInsertedCount() + batch.size());
                    batch.clear();
                }
            }

            // Flush remaining records
            if (!batch.isEmpty()) {
                registeredTitleRepository.saveAll(new ArrayList<>(batch));
                result.setInsertedCount(result.getInsertedCount() + batch.size());
                batch.clear();
            }

            // Refresh BM25 corpus once after entire import completes
            if (bm25SimilarityService != null) {
                bm25SimilarityService.refreshCorpusStats();
            }

            logger.info("Registry CSV import completed: {} processed, {} inserted, {} duplicates, {} invalid, {} errors",
                    result.getTotalProcessed(), result.getInsertedCount(), result.getDuplicateCount(),
                    result.getInvalidCount(), result.getErrorCount());

        } catch (Exception e) {
            logger.error("Unexpected error during registry CSV import: {}", e.getMessage(), e);
            result.incrementErrorCount();
            result.addErrorDetail("Fatal import error: " + e.getMessage());
        }

        return result;
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) {
            return tokens;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Unclosed quotation mark");
        }

        tokens.add(sb.toString().trim());
        return tokens;
    }

    private boolean isHeaderRow(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }
        String first = tokens.get(0).trim().toLowerCase(Locale.ROOT);
        return first.equals("title") || first.equals("publication title") || first.equals("publication_title");
    }

    private String getField(List<String> tokens, int colIdx) {
        if (tokens != null && colIdx >= 0 && colIdx < tokens.size()) {
            String val = tokens.get(colIdx).trim();
            return val.isEmpty() ? null : val;
        }
        return null;
    }
}
