package com.titleverify.titleverify_ai.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SupportedLanguage {

    ENGLISH("en", "en-IN", "English", "English", "English"),
    KANNADA("kn", "kn-IN", "Kannada", "ಕನ್ನಡ", "ಕನ್ನಡ (Kannada)"),
    HINDI("hi", "hi-IN", "Hindi", "हिन्दी", "हिन्दी (Hindi)"),
    TAMIL("ta", "ta-IN", "Tamil", "தமிழ்", "தமிழ் (Tamil)"),
    TELUGU("te", "te-IN", "Telugu", "తెలుగు", "తెలుగు (Telugu)"),
    MALAYALAM("ml", "ml-IN", "Malayalam", "മലയാളം", "മലയാളം (Malayalam)"),
    MARATHI("mr", "mr-IN", "Marathi", "मराठी", "मराठी (Marathi)"),
    BENGALI("bn", "bn-IN", "Bengali", "বাংলা", "বাংলা (Bengali)"),
    GUJARATI("gu", "gu-IN", "Gujarati", "ગુજરાતી", "ગુજરાતી (Gujarati)"),
    PUNJABI("pa", "pa-IN", "Punjabi", "ਪੰਜਾਬੀ", "ਪੰਜਾਬੀ (Punjabi)"),
    URDU("ur", "ur-IN", "Urdu", "اردو", "اردو (Urdu)"),
    ODIA("or", "or-IN", "Odia", "ଓଡ଼ିଆ", "ଓଡ଼ିଆ (Odia)"),
    ASSAMESE("as", "as-IN", "Assamese", "অসমীয়া", "অসমীয়া (Assamese)"),
    ARABIC("ar", "ar-SA", "Arabic", "العربية", "العربية (Arabic)"),
    FRENCH("fr", "fr-FR", "French", "Français", "Français (French)"),
    GERMAN("de", "de-DE", "German", "Deutsch", "Deutsch (German)"),
    SPANISH("es", "es-ES", "Spanish", "Español", "Español (Spanish)"),
    PORTUGUESE("pt", "pt-BR", "Portuguese", "Português", "Português (Portuguese)"),
    ITALIAN("it", "it-IT", "Italian", "Italiano", "Italiano (Italian)"),
    RUSSIAN("ru", "ru-RU", "Russian", "Русский", "Русский (Russian)"),
    JAPANESE("ja", "ja-JP", "Japanese", "日本語", "日本語 (Japanese)"),
    KOREAN("ko", "ko-KR", "Korean", "한국어", "한국어 (Korean)"),
    CHINESE("zh", "zh-CN", "Chinese", "中文", "中文 (Chinese)"),
    INDONESIAN("id", "id-ID", "Indonesian", "Bahasa Indonesia", "Bahasa Indonesia (Indonesian)"),
    VIETNAMESE("vi", "vi-VN", "Vietnamese", "Tiếng Việt", "Tiếng Việt (Vietnamese)"),
    THAI("th", "th-TH", "Thai", "ไทย", "ไทย (Thai)"),
    TURKISH("tr", "tr-TR", "Turkish", "Türkçe", "Türkçe (Turkish)"),
    NEPALI("ne", "ne-NP", "Nepali", "नेपाली", "नेपाली (Nepali)"),
    SINHALA("si", "si-LK", "Sinhala", "සිංහල", "සිංහල (Sinhala)"),
    SWEDISH("sv", "sv-SE", "Swedish", "Svenska", "Svenska (Swedish)"),
    DUTCH("nl", "nl-NL", "Dutch", "Nederlands", "Nederlands (Dutch)"),
    POLISH("pl", "pl-PL", "Polish", "Polski", "Polski (Polish)"),
    ROMANIAN("ro", "ro-RO", "Romanian", "Română", "Română (Romanian)"),
    UKRAINIAN("uk", "uk-UA", "Ukrainian", "Українська", "Українська (Ukrainian)");

    private final String code;
    private final String bcp47;
    private final String englishName;
    private final String nativeName;
    private final String displayName;

    SupportedLanguage(String code, String bcp47, String englishName, String nativeName, String displayName) {
        this.code = code;
        this.bcp47 = bcp47;
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getBcp47() {
        return bcp47;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<SupportedLanguage> getAllSupportedLanguages() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static SupportedLanguage fromCodeOrName(String input) {
        if (input == null || input.isBlank()) {
            return ENGLISH;
        }
        String clean = input.trim().toLowerCase();
        for (SupportedLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(clean) ||
                lang.bcp47.equalsIgnoreCase(clean) ||
                lang.englishName.equalsIgnoreCase(clean) ||
                lang.nativeName.equalsIgnoreCase(clean) ||
                lang.displayName.equalsIgnoreCase(clean) ||
                lang.name().equalsIgnoreCase(clean)) {
                return lang;
            }
        }
        return ENGLISH;
    }
}
