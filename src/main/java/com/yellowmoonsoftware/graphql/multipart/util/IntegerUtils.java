package com.yellowmoonsoftware.graphql.multipart.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * <h2>IntegerUtils</h2>
 * Utility methods for parsing integer values with explicit fallback behavior.
 */
@Slf4j
public final class IntegerUtils {
    private IntegerUtils() {}

    /**
     * Parse a string as an integer, returning {@code null} when the value is not a valid integer.
     * @param value string value to parse
     * @return parsed integer, or {@code null} when parsing fails
     */
    public static Integer parseIntOrNull(final String value) {
        return parseIntOrDefault(value, () -> null);
    }

    /**
     * Parse a string as an integer, returning a fixed default value when parsing fails.
     * @param value string value to parse
     * @param defaultValue value to return when parsing fails
     * @return parsed integer, or {@code defaultValue} when parsing fails
     */
    public static Integer parseIntOrDefault(final String value, final int defaultValue) {
        return parseIntOrDefault(value, () -> defaultValue);
    }

    /**
     * Parse a string as an integer, obtaining a default value from a supplier when parsing fails.
     * @param value string value to parse
     * @param defaultValueSupplier supplier invoked to obtain the value returned when parsing fails
     * @return parsed integer, or the supplied default value when parsing fails
     */
    public static Integer parseIntOrDefault(final String value, final Supplier<Integer> defaultValueSupplier) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            final Integer defaultValue = defaultValueSupplier.get();
            log.warn("Failed to parse integer value [{}] as an integer; returning default value [{}].", value, defaultValue);
            return defaultValue;
        }
    }
}
