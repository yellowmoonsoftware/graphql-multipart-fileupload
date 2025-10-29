package com.yellowmoonsoftware.graphql.multipart.util;

import java.util.function.BinaryOperator;

/**
 * <h2>ExtendedBinaryOperator</h2>
 * Extension of {@link BinaryOperator} with helpers for picking first/last or non-null values.
 * @param <T> operand and result type
 */
public interface ExtendedBinaryOperator<T> extends BinaryOperator<T> {

    /**
     * Return the first argument, ignoring the second.
     * @param <T> operand/result type
     * @return operator that returns the first argument
     */
    static <T> ExtendedBinaryOperator<T> firstArg() {
        return (a, unused) -> a;
    }
    
    /**
     * Return the second argument, ignoring the first.
     * @param <T> operand/result type
     * @return operator that returns the second argument
     */
    static <T> ExtendedBinaryOperator<T> lastArg() {
        return (unused, b) -> b;
    }
    
    /**
     * Return the first non-null argument (or null if both are null).
     * @param <T> operand/result type
     * @return operator returning first non-null value
     */
    static <T> ExtendedBinaryOperator<T> firstNonNull() {
        return (a, b) -> a != null ? a : b;
    }
    
    /**
     * Return the second argument when non-null, otherwise the first.
     * @param <T> operand/result type
     * @return operator returning second if non-null else first
     */
    static <T> ExtendedBinaryOperator<T> lastNonNull() {
        return (a, b) -> b != null ? b : a;
    }

    /**
     * Return the first non-null argument or the provided default when both are null.
     * @param <T> operand/result type
     * @param defaultValue value to return when both args null
     * @return operator returning first non-null or default
     */
    static <T> ExtendedBinaryOperator<T> firstNonNull(final T defaultValue) {
        return (a, b) -> a != null ? a : b != null ? b : defaultValue;
    }

    /**
     * Return the second argument when non-null, otherwise the first, or default when both are null.
     * @param <T> operand/result type
     * @param defaultValue value to return when both args null
     * @return operator returning second if non-null, else first, else default
     */
    static <T> ExtendedBinaryOperator<T> lastNonNull(final T defaultValue) {
        return (a, b) -> b != null ? b : a != null ? a : defaultValue;
    }
}
