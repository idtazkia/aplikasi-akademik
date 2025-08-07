package id.ac.tazkia.smilemahasiswa.util;

import java.util.List;
import java.util.StringJoiner;

/**
 * Utility class for common String operations
 */
public final class StringUtils {

    private StringUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Check if a string is null or empty
     * 
     * @param str string to check
     * @return true if string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string has text (not null and not empty after trimming)
     * 
     * @param str string to check
     * @return true if string has text, false otherwise
     */
    public static boolean hasText(String str) {
        return !isEmpty(str);
    }

    /**
     * Get default value if string is empty
     * 
     * @param str          string to check
     * @param defaultValue default value to return if string is empty
     * @return original string if it has text, default value otherwise
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return hasText(str) ? str : defaultValue;
    }

    /**
     * Join strings with a delimiter
     * 
     * @param delimiter delimiter to use
     * @param elements  strings to join
     * @return joined string
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(delimiter);
        for (String element : elements) {
            if (hasText(element)) {
                joiner.add(element);
            }
        }
        return joiner.toString();
    }

    /**
     * Join list of strings with a delimiter
     * 
     * @param delimiter delimiter to use
     * @param elements  list of strings to join
     * @return joined string
     */
    public static String join(String delimiter, List<String> elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(delimiter);
        for (String element : elements) {
            if (hasText(element)) {
                joiner.add(element);
            }
        }
        return joiner.toString();
    }

    /**
     * Capitalize first letter of a string
     * 
     * @param str string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }

        if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Truncate string to specified length with ellipsis
     * 
     * @param str       string to truncate
     * @param maxLength maximum length
     * @return truncated string with ellipsis if needed
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Remove all whitespace from string
     * 
     * @param str string to process
     * @return string without whitespace
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * Normalize whitespace (replace multiple whitespace with single space)
     * 
     * @param str string to normalize
     * @return normalized string
     */
    public static String normalizeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", " ").trim();
    }

    /**
     * Check if string contains only digits
     * 
     * @param str string to check
     * @return true if string contains only digits, false otherwise
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches("\\d+");
    }

    /**
     * Check if string contains only letters
     * 
     * @param str string to check
     * @return true if string contains only letters, false otherwise
     */
    public static boolean isAlphabetic(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches("[a-zA-Z]+");
    }

    /**
     * Check if string contains only letters and digits
     * 
     * @param str string to check
     * @return true if string contains only letters and digits, false otherwise
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches("[a-zA-Z0-9]+");
    }

    /**
     * Pad string to the left with specified character
     * 
     * @param str     string to pad
     * @param length  target length
     * @param padChar character to pad with
     * @return padded string
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * Pad string to the right with specified character
     * 
     * @param str     string to pad
     * @param length  target length
     * @param padChar character to pad with
     * @return padded string
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < length - str.length(); i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
}
