package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class MapAccessorTest {

    private static final ParameterizedTypeReference<String> STRING_TYPE = new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<Integer> INTEGER_TYPE = new ParameterizedTypeReference<>() { };

    @Test
    void getReturnsValueWhenTypeMatches() {
        final Map<String, Object> map = new HashMap<>();
        map.put("count", 5);
        final MapAccessor<String> accessor = MapAccessor.wrap(map);

        final Integer value = accessor.get("count", INTEGER_TYPE);

        assertThat(value).isEqualTo(5);
    }

    @Test
    void getReturnsDefaultWhenKeyMissing() {
        final MapAccessor<String> accessor = MapAccessor.wrap(new HashMap<>());

        final String value = accessor.get("missing", () -> "fallback", STRING_TYPE);

        assertThat(value).isEqualTo("fallback");
    }

    @Test
    void getReturnsDefaultWhenValueIsWrongType() {
        final Map<String, Object> map = new HashMap<>();
        map.put("answer", "42");
        final MapAccessor<String> accessor = MapAccessor.wrap(map);
        final AtomicInteger defaultCalls = new AtomicInteger();

        final Integer value = accessor.get("answer", () -> {
            defaultCalls.incrementAndGet();
            return 99;
        }, INTEGER_TYPE);

        assertThat(value).isEqualTo(99);
        assertThat(defaultCalls).hasValue(1);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void getReturnsNullWhenValueStoredAsNull() {
        final Map<String, Object> map = new HashMap<>();
        map.put("nullable", null);
        final MapAccessor<String> accessor = MapAccessor.wrap(map);

        final String value = accessor.get("nullable", () -> {
            fail("Default supplier should not be invoked when a key is explicitly mapped to null");
            return "should-not-see";
        }, STRING_TYPE);

        assertThat(value).isNull();
    }

    @Test
    void wrapExposesOriginalMap() {
        final Map<String, Object> map = new HashMap<>();

        final MapAccessor<String> accessor = MapAccessor.wrap(map);

        assertThat(accessor.getMap()).isSameAs(map);
    }
}
