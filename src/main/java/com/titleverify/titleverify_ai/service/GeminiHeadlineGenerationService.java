package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.config.SupportedLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiHeadlineGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiHeadlineGenerationService.class);
    private static final String DEFAULT_GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    @Autowired
    public GeminiHeadlineGenerationService(@Value("${titleverify.headline.model:gemini-3.6-flash}") String model,
                                            @Value("${GEMINI_API_KEY:}") String apiKey) {
        this(RestClient.create(), model, apiKey);
    }

    public GeminiHeadlineGenerationService(RestClient restClient,
                                            String model,
                                            String apiKey) {
        this.restClient = restClient;
        this.model = (model != null && !model.isBlank()) ? model : "gemini-3.6-flash";
        String resolvedKey = (apiKey != null && !apiKey.isBlank()) ? apiKey : System.getenv("GEMINI_API_KEY");
        this.apiKey = (resolvedKey != null) ? resolvedKey.trim() : "";
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }

    public String getEndpoint() {
        if ("gemini-3.6-flash".equals(model)) {
            return DEFAULT_GEMINI_ENDPOINT;
        }
        String fullModelName = model.startsWith("models/") ? model : "models/" + model;
        return "https://generativelanguage.googleapis.com/v1beta/" + fullModelName + ":generateContent";
    }

    public List<String> generateHeadlines(String articleContent) {
        return generateHeadlines(articleContent, "English");
    }

    public List<String> generateHeadlines(String articleContent, String languageInput) {
        if (articleContent == null || articleContent.trim().length() < 10) {
            throw new IllegalArgumentException("Article content must be at least 10 characters long.");
        }
        if (articleContent.length() > 5000) {
            throw new IllegalArgumentException("Article content exceeds maximum length of 5000 characters.");
        }

        SupportedLanguage targetLanguage = SupportedLanguage.fromCodeOrName(languageInput);

        if (!isAvailable()) {
            logger.warn("Gemini API key is not configured. Falling back to deterministic content headline extraction for language: {}.", targetLanguage.getDisplayName());
            return generateFallbackHeadlines(articleContent, targetLanguage);
        }

        try {
            // Attempt 1: Standard Multilingual Prompt
            List<String> headlines = invokeGeminiAndParseHeadlines(articleContent, targetLanguage, false);

            // Lightweight Language Validation
            if (!validateLanguageSanity(headlines, targetLanguage)) {
                logger.warn("Gemini response failed language sanity check for target language {}. Retrying once with reinforced instructions.", targetLanguage.getDisplayName());
                // Attempt 2: Retry with reinforced prompt instruction
                headlines = invokeGeminiAndParseHeadlines(articleContent, targetLanguage, true);
                if (!validateLanguageSanity(headlines, targetLanguage)) {
                    logger.error("Gemini headline generation failed language sanity check twice for target language {}.", targetLanguage.getDisplayName());
                    throw new IllegalStateException("Generated headlines did not match the requested output language (" + targetLanguage.getDisplayName() + "). Please try again.");
                }
            }

            return headlines;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Gemini API invocation error: {}", e.getMessage());
            throw new IllegalStateException("Failed to generate headlines from Gemini service. Please check API configuration or try again.");
        }
    }

    private List<String> invokeGeminiAndParseHeadlines(String articleContent, SupportedLanguage targetLanguage, boolean isRetry) {
        String prompt = buildPrompt(articleContent, targetLanguage, isRetry);
        Map<String, Object> requestBody = createRequestPayload(prompt);
        String endpoint = getEndpoint();

        Map<?, ?> response = restClient.post()
                .uri(endpoint)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        String text = extractTextFromGeminiResponse(response);
        if (text == null || text.isBlank()) {
            logger.error("Gemini API returned an empty or malformed text response.");
            throw new IllegalStateException("Gemini returned an empty response. Please try again.");
        }

        List<String> headlines = parseHeadlinesFromJson(text);
        if (headlines.isEmpty()) {
            throw new IllegalStateException("Failed to generate valid headlines from Gemini response.");
        }
        return headlines;
    }

    private String buildPrompt(String content, SupportedLanguage targetLanguage, boolean isRetry) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior professional newspaper editor.\n");
        sb.append("Read and analyze the following COMPLETE news topic or article carefully:\n\n");
        sb.append("--- ARTICLE START ---\n");
        sb.append(content).append("\n");
        sb.append("--- ARTICLE END ---\n\n");

        sb.append("INSTRUCTIONS:\n");
        sb.append("1. Read and understand the entire supplied content.\n");
        sb.append("2. Identify the central event or story, key facts, people, organizations, locations, dates, and numbers.\n");
        sb.append("3. Generate exactly 5 professional newspaper headlines.\n");
        sb.append("4. The required output language is: ").append(targetLanguage.getEnglishName());
        if (!targetLanguage.getEnglishName().equals(targetLanguage.getNativeName())) {
            sb.append(" (").append(targetLanguage.getNativeName()).append(")");
        }
        sb.append(".\n");
        sb.append("5. The required language code is: ").append(targetLanguage.getCode()).append(" (BCP-47: ").append(targetLanguage.getBcp47()).append(").\n");
        sb.append("6. ALL five headlines MUST be written in ").append(targetLanguage.getEnglishName()).append(". Do NOT translate into English unless English is the requested language.\n");
        sb.append("7. Preserve important proper nouns (such as location names e.g., Mysuru, Bengaluru, Karnataka, person names, and organization names) appropriately without reducing accuracy.\n");
        sb.append("8. Use ONLY information supported by the supplied article. Do not invent facts, names, numbers, quotes, events, causes, or outcomes.\n");
        sb.append("9. Do not mix languages unnecessarily.\n");

        if (isRetry && targetLanguage != SupportedLanguage.ENGLISH) {
            sb.append("10. CRITICAL MANDATORY INSTRUCTION: Your previous output failed because it contained English text. You MUST write all 5 headlines strictly in ").append(targetLanguage.getEnglishName()).append(" script (").append(targetLanguage.getNativeName()).append("). Do NOT output English words unless they are essential proper nouns.\n");
        }

        sb.append("11. Return ONLY a valid JSON array containing exactly 5 string elements in the requested language, with no extra text or markdown outside the array.\n\n");

        sb.append("Example JSON response format:\n[\n  \"Headline Option 1 in ").append(targetLanguage.getEnglishName()).append("\",\n");
        sb.append("  \"Headline Option 2 in ").append(targetLanguage.getEnglishName()).append("\",\n");
        sb.append("  \"Headline Option 3 in ").append(targetLanguage.getEnglishName()).append("\",\n");
        sb.append("  \"Headline Option 4 in ").append(targetLanguage.getEnglishName()).append("\",\n");
        sb.append("  \"Headline Option 5 in ").append(targetLanguage.getEnglishName()).append("\"\n]\n");

        return sb.toString();
    }

    /**
     * Lightweight language sanity check to detect obvious wrong-language responses (e.g. English returned when Kannada/Hindi requested).
     */
    public boolean validateLanguageSanity(List<String> headlines, SupportedLanguage targetLanguage) {
        if (headlines == null || headlines.isEmpty()) {
            return false;
        }
        if (targetLanguage == SupportedLanguage.ENGLISH) {
            return true;
        }

        // For non-English target languages, check if non-Latin characters are present in the response
        boolean hasNonLatinChar = false;
        for (String headline : headlines) {
            if (headline == null) continue;
            for (char c : headline.toCharArray()) {
                // If character code > 127 (beyond standard ASCII/Latin), it contains script characters
                if (c > 127) {
                    hasNonLatinChar = true;
                    break;
                }
            }
            if (hasNonLatinChar) break;
        }

        // If the target language uses non-Latin script and response is 100% ASCII Latin, validation fails.
        return hasNonLatinChar;
    }

    public Map<String, Object> createRequestPayload(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"
                )
        );
    }

    @SuppressWarnings("unchecked")
    public String extractTextFromGeminiResponse(Map<?, ?> response) {
        if (response == null) return null;
        List<?> candidates = (List<?>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;

        Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
        if (content == null) return null;

        List<?> parts = (List<?>) content.get("parts");
        if (parts == null || parts.isEmpty()) return null;

        Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
        return (String) firstPart.get("text");
    }

    public List<String> parseHeadlinesFromJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return List.of();
        String cleaned = rawJson.trim();

        // Strip ```json ... ``` markdown block if present
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            int lastBackticks = cleaned.lastIndexOf("```");
            if (firstNewline != -1 && lastBackticks > firstNewline) {
                cleaned = cleaned.substring(firstNewline + 1, lastBackticks).trim();
            }
        }

        List<String> headlines = new ArrayList<>();
        // Match JSON string elements inside array
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(cleaned);
        while (matcher.find()) {
            String val = matcher.group(1).trim();
            if (!val.isEmpty() && !val.equals("json") && !val.toLowerCase().startsWith("headline option")) {
                // Unescape basic json escapes
                val = val.replace("\\\"", "\"").replace("\\\\", "\\");
                headlines.add(val);
            }
        }

        if (headlines.size() < 5) {
            String[] lines = cleaned.split("\r?\n");
            for (String line : lines) {
                String trimmed = line.replaceAll("^[0-9]+[\\.\\)]\\s*", "")
                        .replaceAll("^[\"*\\-\\s]+|[\"*\\-\\s]+$", "")
                        .trim();
                if (trimmed.length() >= 5 && trimmed.length() <= 150 && !headlines.contains(trimmed)) {
                    headlines.add(trimmed);
                    if (headlines.size() == 5) break;
                }
            }
        }

        return headlines;
    }

    public List<String> generateFallbackHeadlines(String articleContent) {
        return generateFallbackHeadlines(articleContent, SupportedLanguage.ENGLISH);
    }

    /**
     * Fallback headline generator used in testing or offline environments without an active API key.
     */
    public List<String> generateFallbackHeadlines(String articleContent, SupportedLanguage targetLanguage) {
        String clean = articleContent.replaceAll("\\s+", " ").trim();
        String[] words = clean.split(" ");

        String base = words.length > 5 ? String.join(" ", Arrays.copyOfRange(words, 0, Math.min(words.length, 6))) : clean;
        base = base.replaceAll("[^a-zA-Z0-9\\u0C80-\\u0CFF\\u0900-\\u097F\\u0B80-\\u0BFF ]", "").trim();
        if (base.isEmpty()) {
            base = "News Article Highlights";
        }

        String langTag = (targetLanguage != null && targetLanguage != SupportedLanguage.ENGLISH)
                ? targetLanguage.getNativeName() + " - "
                : "";

        return List.of(
                langTag + base + " Special Report",
                langTag + "Latest Updates On " + base,
                langTag + base + " In Focus",
                langTag + "Official Briefing: " + base,
                langTag + base + " Chronicle"
        );
    }
}
