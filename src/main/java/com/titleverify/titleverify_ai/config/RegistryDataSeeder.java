package com.titleverify.titleverify_ai.config;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import com.titleverify.titleverify_ai.service.TitleNormalizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * DEMO Registry Dataset Seeder for TitleVerify AI.
 * 
 * Seeding Mechanism:
 * - Runs automatically on Spring Boot application startup.
 * - Idempotent: Checks per-title normalized existence to avoid duplicate
 * records across restarts.
 * - Additive: Only inserts missing demo records without deleting or altering
 * existing user/registry data.
 * - All seeded titles are explicitly marked with status = "DEMO_DATA".
 * 
 * DEMO TEST SCENARIOS DOCUMENTATION:
 * --------------------------------------------------------------------------------------------------
 * SCENARIO 1 — Exact Conflict
 * Existing Title : "Namaskar News"
 * Proposed Title : "Namaskar News"
 * Expected Result: EXACT_MATCH_FOUND -> HIGH RISK decision.
 * 
 * SCENARIO 2 — Spelling/Fuzzy Conflict
 * Existing Title : "Namaskar News"
 * Proposed Title : "Namascar News"
 * Expected Result: High fuzzy / phonetic similarity signal detected.
 * 
 * SCENARIO 3 — Semantic Similarity (Real Gemini Embeddings)
 * Existing Titles: "Daily Evening News", "Evening Bulletin"
 * Proposed Title : "Evening Daily Bulletin"
 * Expected Result: Elevated semantic similarity score via REAL Gemini
 * embeddings.
 * 
 * SCENARIO 4 — Periodicity Modifier
 * Existing Title : "India News"
 * Proposed Title : "India News Daily"
 * Expected Result: PERIODICITY_MODIFIER rule warning triggered.
 * 
 * SCENARIO 5 — Prefix/Suffix Modification
 * Existing Title : "India Samachar"
 * Proposed Title : "The India Samachar"
 * Expected Result: PREFIX_SUFFIX rule warning triggered ("the").
 * 
 * SCENARIO 6 — Combination Detection
 * Existing Titles: "Hindu", "Indian Express"
 * Proposed Title : "Hindu Indian Express"
 * Expected Result: COMBINATION rule warning triggered.
 * 
 * SCENARIO 7 — Restricted Word Rule
 * Proposed Title : "Crime India News"
 * Expected Result: PROHIBITED_WORD rule triggered with HIGH severity.
 * 
 * SCENARIO 8 — Clean / Low-Risk Title
 * Proposed Title : "Mysuru Green Agriculture"
 * Expected Result: Low similarity across registered dataset, ACCEPT / LOW RISK
 * decision.
 * --------------------------------------------------------------------------------------------------
 */
@Component
public class RegistryDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RegistryDataSeeder.class);

    private final RegisteredPublicationTitleRepository repository;
    private final TitleNormalizationService normalizationService;

    public RegistryDataSeeder(RegisteredPublicationTitleRepository repository,
            TitleNormalizationService normalizationService) {
        this.repository = repository;
        this.normalizationService = normalizationService;
    }

    @Override
    public void run(String... args) {
        log.info("Checking RegisteredPublicationTitle demo dataset...");

        List<SeedItem> seedItems = Arrays.asList(
                // Exact Conflict & Fuzzy Set
                new SeedItem("Namaskar News", "Hindi", "Uttar Pradesh", "Daily", "Newspaper"),
                new SeedItem("Mysuru Chronicle", "English", "Karnataka", "Weekly", "Newspaper"),
                new SeedItem("Karnataka Herald", "English", "Karnataka", "Daily", "Newspaper"),
                new SeedItem("India Samachar", "Hindi", "Delhi", "Daily", "Newspaper"),

                // Phonetic Similarity Set
                new SeedItem("Colour News", "English", "Delhi", "Daily", "Newspaper"),

                // Semantic Similarity Set
                new SeedItem("Daily Evening News", "English", "Maharashtra", "Daily", "Newspaper"),
                new SeedItem("Evening Bulletin", "English", "Maharashtra", "Daily", "Newspaper"),
                new SeedItem("Southern Morning Chronicle", "English", "Tamil Nadu", "Daily", "Newspaper"),

                // Periodicity Modifier Set
                new SeedItem("India News", "English", "Delhi", "Daily", "Newspaper"),
                new SeedItem("Halli Katte", "Kannada", "Karnataka", "Daily", "Newspaper"),
                new SeedItem("ನಮಸ್ಕಾರ ಹಿಂದೂ", "Kannada", "Karnataka", "Daily", "Newspaper"),

                // Combination Detection Set
                new SeedItem("Hindu", "English", "Tamil Nadu", "Daily", "Newspaper"),
                new SeedItem("Indian Express", "English", "Maharashtra", "Daily", "Newspaper"),

                // Clean / Low-Risk Dataset Records
                new SeedItem("Western Lakes Journal", "English", "Kerala", "Quarterly", "Journal"),
                new SeedItem("Coastal Heritage Review", "English", "Goa", "Monthly", "Magazine"),
                new SeedItem("Southern Innovation Weekly", "English", "Karnataka", "Weekly", "Magazine"),
                new SeedItem("Mysuru Agriculture Review", "English", "Karnataka", "Monthly", "Journal"));

        int insertedCount = 0;
        for (SeedItem item : seedItems) {
            String norm = normalizationService.normalize(item.title);
            if (repository.findByNormalizedTitle(norm).isEmpty()) {
                RegisteredPublicationTitle entity = new RegisteredPublicationTitle(
                        item.title,
                        norm,
                        item.language,
                        item.state,
                        item.periodicity,
                        item.publicationType,
                        "DEMO_DATA");
                repository.save(entity);
                insertedCount++;
            }
        }

        if (insertedCount > 0) {
            log.info("Successfully seeded {} new DEMO_DATA publication titles into registry.", insertedCount);
        } else {
            log.info("All DEMO_DATA publication titles are already present in the registry. No new records inserted.");
        }
    }

    private static class SeedItem {
        String title;
        String language;
        String state;
        String periodicity;
        String publicationType;

        SeedItem(String title, String language, String state, String periodicity, String publicationType) {
            this.title = title;
            this.language = language;
            this.state = state;
            this.periodicity = periodicity;
            this.publicationType = publicationType;
        }
    }
}
