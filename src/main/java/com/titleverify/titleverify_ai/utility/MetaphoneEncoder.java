package com.titleverify.titleverify_ai.utility;

import java.util.Locale;

public class MetaphoneEncoder {

    private static final String VOWELS = "AEIOU";

    public String encode(String word) {
        if (word == null) {
            return "";
        }
        String txt = word.trim().toUpperCase(Locale.ROOT);
        if (txt.isEmpty()) {
            return "";
        }

        // Clean non-letters
        txt = txt.replaceAll("[^A-Z]", "");
        if (txt.isEmpty()) {
            return "";
        }

        // Handle initial letters
        if (txt.startsWith("KN") || txt.startsWith("GN") || txt.startsWith("PN") || txt.startsWith("AE") || txt.startsWith("WR")) {
            txt = txt.substring(1);
        } else if (txt.startsWith("X")) {
            txt = "S" + txt.substring(1);
        } else if (txt.startsWith("WH")) {
            txt = "W" + txt.substring(2);
        }

        int length = txt.length();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = txt.charAt(i);
            // Drop duplicate adjacent letters (except C)
            if (i > 0 && c == txt.charAt(i - 1) && c != 'C') {
                continue;
            }

            if (isVowel(c)) {
                // Keep vowel only if it is the first letter
                if (i == 0) {
                    code.append(c);
                }
                continue;
            }

            switch (c) {
                case 'B':
                    if (i == length - 1 && i > 0 && txt.charAt(i - 1) == 'M') {
                        break;
                    }
                    code.append('B');
                    break;
                case 'C':
                    if (i > 0 && txt.charAt(i - 1) == 'S' && isSoftVowel(txt, i + 1)) {
                        break;
                    }
                    if (regionMatches(txt, i, "CIA")) {
                        code.append('X');
                    } else if (i + 1 < length && txt.charAt(i + 1) == 'H') {
                        if (i == 0 && length > 2 && !isVowel(txt.charAt(2))) {
                            code.append('K');
                        } else {
                            code.append('X');
                        }
                    } else if (isSoftVowel(txt, i + 1)) {
                        code.append('S');
                    } else {
                        code.append('K');
                    }
                    break;
                case 'D':
                    if (i + 2 < length && txt.charAt(i + 1) == 'G' && isSoftVowel(txt, i + 2)) {
                        code.append('J');
                    } else {
                        code.append('T');
                    }
                    break;
                case 'F':
                    code.append('F');
                    break;
                case 'G':
                    if (i + 1 < length && txt.charAt(i + 1) == 'H' && !isVowel(txt.charAt(Math.min(i + 2, length - 1)))) {
                        break;
                    }
                    if (i + 1 < length && txt.charAt(i + 1) == 'N' && i + 1 == length - 1) {
                        break;
                    }
                    if (isSoftVowel(txt, i + 1) && (i == 0 || txt.charAt(i - 1) != 'G')) {
                        code.append('J');
                    } else {
                        code.append('K');
                    }
                    break;
                case 'H':
                    if (i > 0 && isConsonant(txt.charAt(i - 1))) {
                        break;
                    }
                    if (i + 1 < length && isVowel(txt.charAt(i + 1))) {
                        code.append('H');
                    }
                    break;
                case 'J':
                    code.append('J');
                    break;
                case 'K':
                    if (i > 0 && txt.charAt(i - 1) == 'C') {
                        break;
                    }
                    code.append('K');
                    break;
                case 'L':
                case 'M':
                case 'N':
                case 'R':
                    code.append(c);
                    break;
                case 'P':
                    if (i + 1 < length && txt.charAt(i + 1) == 'H') {
                        code.append('F');
                    } else {
                        code.append('P');
                    }
                    break;
                case 'Q':
                    code.append('K');
                    break;
                case 'S':
                    if (regionMatches(txt, i, "SH") || regionMatches(txt, i, "SIO") || regionMatches(txt, i, "SIA")) {
                        code.append('X');
                    } else {
                        code.append('S');
                    }
                    break;
                case 'T':
                    if (regionMatches(txt, i, "TIA") || regionMatches(txt, i, "TIO")) {
                        code.append('X');
                    } else if (i + 1 < length && txt.charAt(i + 1) == 'H') {
                        code.append('0');
                    } else if (regionMatches(txt, i, "TCH")) {
                        break;
                    } else {
                        code.append('T');
                    }
                    break;
                case 'V':
                    code.append('F');
                    break;
                case 'W':
                case 'Y':
                    if (i + 1 < length && isVowel(txt.charAt(i + 1))) {
                        code.append(c);
                    }
                    break;
                case 'X':
                    code.append("KS");
                    break;
                case 'Z':
                    code.append('S');
                    break;
                default:
                    break;
            }
        }
        return code.toString();
    }

    private boolean isVowel(char c) {
        return VOWELS.indexOf(c) >= 0;
    }

    private boolean isConsonant(char c) {
        return Character.isLetter(c) && !isVowel(c);
    }

    private boolean isSoftVowel(String str, int index) {
        if (index >= str.length()) return false;
        char c = str.charAt(index);
        return c == 'E' || c == 'I' || c == 'Y';
    }

    private boolean regionMatches(String str, int index, String target) {
        if (index < 0 || index + target.length() > str.length()) {
            return false;
        }
        return str.substring(index, index + target.length()).equals(target);
    }
}
