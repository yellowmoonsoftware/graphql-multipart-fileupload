package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerUtilsTest {

    @Test
    void parseIntOrNullReturnsParsedInteger() {
        assertThat(IntegerUtils.parseIntOrNull("42")).isEqualTo(42);
    }

    @Test
    void parseIntOrNullReturnsNullWhenValueIsInvalid() {
        assertThat(IntegerUtils.parseIntOrNull("not-an-integer")).isNull();
    }

    @Test
    void parseIntOrNullReturnsNullWhenValueIsNull() {
        assertThat(IntegerUtils.parseIntOrNull(null)).isNull();
    }

    @Test
    void parseIntOrDefaultReturnsFixedDefaultWhenValueIsInvalid() {
        assertThat(IntegerUtils.parseIntOrDefault("2147483648", 99)).isEqualTo(99);
    }

    @Test
    void parseIntOrDefaultInvokesSupplierOnlyWhenParsingFails() {
        final AtomicInteger supplierCalls = new AtomicInteger();

        final Integer parsed = IntegerUtils.parseIntOrDefault("7", () -> {
            supplierCalls.incrementAndGet();
            return 99;
        });
        final Integer defaultValue = IntegerUtils.parseIntOrDefault("invalid", () -> {
            supplierCalls.incrementAndGet();
            return 99;
        });

        assertThat(parsed).isEqualTo(7);
        assertThat(defaultValue).isEqualTo(99);
        assertThat(supplierCalls).hasValue(1);
    }
}
