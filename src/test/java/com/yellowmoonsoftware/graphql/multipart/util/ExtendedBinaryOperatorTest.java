package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtendedBinaryOperatorTest {

    @Test
    void testFirstArg() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstArg();
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testLastArg() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastArg();
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testFirstNonNull_BothNonNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull();
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testFirstNonNull_FirstIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull();
        final String result = operator.apply(null, "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testFirstNonNull_SecondIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull();
        final String result = operator.apply("first", null);
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testFirstNonNull_BothNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull();
        final String result = operator.apply(null, null);
        assertThat(result).isNull();
    }

    @Test
    void testLastNonNull_BothNonNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull();
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testLastNonNull_FirstIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull();
        final String result = operator.apply(null, "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testLastNonNull_SecondIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull();
        final String result = operator.apply("first", null);
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testLastNonNull_BothNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull();
        final String result = operator.apply(null, null);
        assertThat(result).isNull();
    }

    @Test
    void testFirstNonNullWithDefaultValue_AllNonNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull("default");
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testFirstNonNullWithDefaultValue_FirstIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull("default");
        final String result = operator.apply(null, "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testFirstNonNullWithDefaultValue_BothAreNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.firstNonNull("default");
        final String result = operator.apply(null, null);
        assertThat(result).isEqualTo("default");
    }

    @Test
    void testLastNonNullWithDefaultValue_AllNonNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull("default");
        final String result = operator.apply("first", "second");
        assertThat(result).isEqualTo("second");
    }

    @Test
    void testLastNonNullWithDefaultValue_SecondIsNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull("default");
        final String result = operator.apply("first", null);
        assertThat(result).isEqualTo("first");
    }

    @Test
    void testLastNonNullWithDefaultValue_BothAreNull() {
        final ExtendedBinaryOperator<String> operator = ExtendedBinaryOperator.lastNonNull("default");
        final String result = operator.apply(null, null);
        assertThat(result).isEqualTo("default");
    }
}