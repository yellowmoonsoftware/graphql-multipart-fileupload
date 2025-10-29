package com.yellowmoonsoftware.graphql.multipart.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectGraphPathTest {

    @Test
    void fromSplitsPathIntoSegmentsAndKey() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.input.file");

        assertThat(path.pathSegments()).containsExactly("variables", "input");
        assertThat(path.key()).isEqualTo("file");
        assertThat(path.isValid()).isTrue();
    }

    @Test
    void fromHandlesSingleSegment() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables");

        assertThat(path.pathSegments()).isEmpty();
        assertThat(path.key()).isEqualTo("variables");
        assertThat(path.isValid()).isTrue();
    }

    @Test
    void fromReturnsInvalidPathWhenEmptyOrNull() {
        final ObjectGraphPath nullPath = ObjectGraphPath.from(null);
        final ObjectGraphPath emptyPath = ObjectGraphPath.from("");

        assertThat(nullPath.pathSegments()).isEmpty();
        assertThat(nullPath.key()).isNull();
        assertThat(nullPath.isValid()).isFalse();

        assertThat(emptyPath.pathSegments()).isEmpty();
        assertThat(emptyPath.key()).isNull();
        assertThat(emptyPath.isValid()).isFalse();
    }

    @Test
    void pathSegmentsAreImmutable() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.input.file");

        assertThatThrownBy(() -> path.pathSegments().add("new"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void equalityDependsOnSegmentsAndKey() {
        final ObjectGraphPath first = ObjectGraphPath.from("variables.input.file");
        final ObjectGraphPath same = ObjectGraphPath.from("variables.input.file");
        final ObjectGraphPath differentKey = ObjectGraphPath.from("variables.input.other");

        assertThat(first).isEqualTo(same);
        assertThat(first).hasSameHashCodeAs(same);
        assertThat(first).isNotEqualTo(differentKey);
    }

    @Test
    void toStringIncludesKeyAndSegmentsWhenValid() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.input.file");

        assertThat(path.toString()).isEqualTo("$variables.input[file] VALID");
    }

    @Test
    void toStringReflectsNullKeyWithValidSegments() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables");

        assertThat(path.toString()).isEqualTo("$[variables] VALID");
    }

    @Test
    void toStringReflectsNullKeyAndEmptySegments() {
        final ObjectGraphPath path = ObjectGraphPath.from(null);

        assertThat(path.toString()).isEqualTo("$[] INVALID");
    }
}
