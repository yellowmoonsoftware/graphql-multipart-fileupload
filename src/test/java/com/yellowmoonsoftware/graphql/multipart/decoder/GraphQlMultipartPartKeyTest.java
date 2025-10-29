package com.yellowmoonsoftware.graphql.multipart.decoder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartPartKeyTest {
    @Test
    void enumExposesExpectedMetadata() {
        assertThat(GraphQlMultipartPartKey.OPERATIONS.getKeyName())
                .isEqualTo("operations");
        assertThat(GraphQlMultipartPartKey.MAP.getKeyName())
                .isEqualTo("map");

        assertThat(GraphQlMultipartPartKey.OPERATIONS.getTypeRef().getType())
                .hasToString("java.util.Map<java.lang.String, java.lang.Object>");
        assertThat(GraphQlMultipartPartKey.MAP.getTypeRef().getType())
                .hasToString("java.util.Map<java.lang.String, java.util.Set<com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath>>");
    }
}