package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.config.SupportedLanguage;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationRequestDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationResponseDto;
import com.titleverify.titleverify_ai.dto.TitleVerificationResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultilingualHeadlineGeneratorTest {

    private GeminiHeadlineGenerationService geminiService;
    private VerificationService verificationService;
    private ContentRelevanceService contentRelevanceService;
    private HeadlineRecommendationService recommendationService;
    private HeadlineGeneratorService headlineGeneratorService;

    @BeforeEach
    void setUp() {
        geminiService = Mockito.mock(GeminiHeadlineGenerationService.class);
        verificationService = Mockito.mock(VerificationService.class);
        contentRelevanceService = new ContentRelevanceService();
        recommendationService = new HeadlineRecommendationService();

        headlineGeneratorService = new HeadlineGeneratorService(
                geminiService,
                verificationService,
                contentRelevanceService,
                recommendationService
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "English", "Kannada", "Hindi", "Tamil", "Telugu", "Malayalam",
            "Marathi", "Bengali", "Gujarati", "Punjabi", "Urdu", "Odia", "Assamese"
    })
    void testMultilingualLanguageMatrix(String languageName) {
        String article = "Mysuru City Corporation launches a waste segregation initiative across residential areas.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                article, "Newspaper", languageName, "Karnataka", "Mysuru", "Daily"
        );

        SupportedLanguage targetLang = SupportedLanguage.fromCodeOrName(languageName);

        // Native mock headlines based on language for test assertion
        List<String> mockHeadlines = List.of(
                languageName + " Headline 1 - Mysuru Waste Segregation",
                languageName + " Headline 2 - City Corporation Drive",
                languageName + " Headline 3 - Wet and Dry Waste Initiative",
                languageName + " Headline 4 - Resident Awareness Campaign",
                languageName + " Headline 5 - Recycling Process Starts"
        );

        when(geminiService.generateHeadlines(eq(article), eq(languageName))).thenReturn(mockHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(10.0);
        mockV.setRiskLevel("LOW RISK");
        mockV.setApprovalConfidence(90.0);

        when(verificationService.verifyProposedTitle(anyString(), eq("Newspaper"), eq(languageName), eq("Karnataka"), eq("Mysuru"), eq("Daily")))
                .thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertNotNull(response.getRecommendedHeadline());

        // Verify language reached VerificationService
        for (String h : mockHeadlines) {
            verify(verificationService).verifyProposedTitle(h, "Newspaper", languageName, "Karnataka", "Mysuru", "Daily");
        }
    }

    @Test
    void testCrossLanguageEnglishArticleToKannadaHeadlines() {
        String englishArticle = "Mysuru City Corporation launches new waste segregation awareness programme in residential areas.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                englishArticle, "Newspaper", "Kannada", "Karnataka", "Mysuru", "Daily"
        );

        List<String> mockKannadaHeadlines = List.of(
                "ಮೈಸೂರು ನಗರ ಪಾಲಿಕೆಯಿಂದ ಕಸ ವಿಂಗಡಣೆ ಜಾಗೃತಿ ಅಭಿಯಾನ",
                "ಮನೆ ಮನೆಗೆ ತೆರಳಿ ಹಸಿ ಮತ್ತು ಒಣ ಕಸ ಬೇರ್ಪಡಿಸುವಿಕೆ ಪ್ರಚಾರ",
                "ಮೈಸೂರು ತ್ಯಾಜ್ಯ ನಿರ್ವಹಣೆ ಪರಿಶೀಲನೆ ನಂತರ ಹೊಸ ಉದ್ಯಮ",
                "ಸಾರ್ವಜನಿಕ ಸಹಕಾರ ಕೋರಿದ ಮೈಸೂರು ಮಹಾನಗರ ಪಾಲಿಕೆ",
                "ತ್ಯಾಜ್ಯ ಮರುಬಳಕೆಗೆ ಒತ್ತು ನೀಡಲು ಪರಿಸರ ಗುಂಪುಗಳ ಸ್ವಾಗತ"
        );

        when(geminiService.generateHeadlines(englishArticle, "Kannada")).thenReturn(mockKannadaHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(12.0);
        mockV.setRiskLevel("LOW RISK");

        when(verificationService.verifyProposedTitle(anyString(), eq("Newspaper"), eq("Kannada"), eq("Karnataka"), eq("Mysuru"), eq("Daily")))
                .thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertEquals("ಮೈಸೂರು ನಗರ ಪಾಲಿಕೆಯಿಂದ ಕಸ ವಿಂಗಡಣೆ ಜಾಗೃತಿ ಅಭಿಯಾನ", response.getResults().get(0).getGeneratedHeadline());

        verify(geminiService).generateHeadlines(englishArticle, "Kannada");
    }

    @Test
    void testCrossLanguageKannadaArticleToEnglishHeadlines() {
        String kannadaArticle = "ಮೈಸೂರು ನಗರ ಪಾಲಿಕೆಯು ವಸತಿ ಪ್ರದೇಶಗಳಲ್ಲಿ ಕಸ ವಿಂಗಡಣೆ ಜಾಗೃತಿ ಕಾರ್ಯಕ್ರಮವನ್ನು ಪ್ರಾರಂಭಿಸಿದೆ.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                kannadaArticle, "Newspaper", "English", "Karnataka", "Mysuru", "Daily"
        );

        List<String> mockEnglishHeadlines = List.of(
                "Mysuru Corporation Launches Waste Segregation Awareness Drive",
                "Residential Wet and Dry Waste Separation Campaign in Mysuru",
                "Civic Body Enlists Community Support for Recycling",
                "Mysuru Waste Management Initiative Focuses on Household Action",
                "Environmental Groups Welcome Mysuru Cleanliness Campaign"
        );

        when(geminiService.generateHeadlines(kannadaArticle, "English")).thenReturn(mockEnglishHeadlines);

        TitleVerificationResultDto mockV = new TitleVerificationResultDto();
        mockV.setFinalDecision("ACCEPT");
        mockV.setRiskScore(15.0);

        when(verificationService.verifyProposedTitle(anyString(), eq("Newspaper"), eq("English"), eq("Karnataka"), eq("Mysuru"), eq("Daily")))
                .thenReturn(mockV);

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(5, response.getResults().size());
        assertEquals("Mysuru Corporation Launches Waste Segregation Awareness Drive", response.getResults().get(0).getGeneratedHeadline());

        verify(geminiService).generateHeadlines(kannadaArticle, "English");
    }

    @Test
    void testWrongLanguageValidationFailureReturnsError() {
        String article = "Karnataka government launches digital media initiative.";
        HeadlineGenerationRequestDto request = new HeadlineGenerationRequestDto(
                article, "Newspaper", "Kannada", "Karnataka", "Bengaluru Urban", "Daily"
        );

        when(geminiService.generateHeadlines(article, "Kannada"))
                .thenThrow(new IllegalStateException("Generated headlines did not match the requested output language (ಕನ್ನಡ (Kannada)). Please try again."));

        HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(request);

        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getMessage().contains("did not match the requested output language"));
    }

    @Test
    void testSupportedLanguageEnumLookupAndDefaults() {
        assertEquals(SupportedLanguage.KANNADA, SupportedLanguage.fromCodeOrName("kn"));
        assertEquals(SupportedLanguage.KANNADA, SupportedLanguage.fromCodeOrName("Kannada"));
        assertEquals(SupportedLanguage.HINDI, SupportedLanguage.fromCodeOrName("hi-IN"));
        assertEquals(SupportedLanguage.JAPANESE, SupportedLanguage.fromCodeOrName("ja"));
        assertEquals(SupportedLanguage.ENGLISH, SupportedLanguage.fromCodeOrName("unknown_language"));
        assertEquals(34, SupportedLanguage.getAllSupportedLanguages().size());
    }
}
