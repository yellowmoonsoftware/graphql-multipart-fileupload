package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GqlTestData;
import com.yellowmoonsoftware.graphql.multipart.MockFormFieldPart;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartJsonDecoderTest {
    JacksonJsonDecoder decoder;

    GraphQlMultipartJsonDecoder mpGqlJsonDecoder;

    @BeforeEach
    void setup() {
        decoder = new JacksonJsonDecoder();
        mpGqlJsonDecoder = new GraphQlMultipartJsonDecoder(decoder);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDecodesOperations() {
        final Mono<Map<String, Object>> actual = mpGqlJsonDecoder.decodePart(
                new MockFormFieldPart(GraphQlMultipartPartKey.OPERATIONS.getKeyName(), GqlTestData.getTestOperationsJson()),
                GraphQlMultipartPartKey.OPERATIONS.getTypeRef());

        final Map<String, Object> expectedOperations = new JsonMapper().readValue(
                GqlTestData.getTestOperationsJson(), new TypeReference<>() { });
        final String expectedQuery = (String) expectedOperations.get("query");
        final Map<String, Object> expectedVars = (Map<String, Object>) expectedOperations.get("variables");

        StepVerifier.create(actual)
                .assertNext(ops -> {
                    assertThat(ops.keySet()).containsExactlyInAnyOrder("query", "variables");
                    assertThat(ops.get("query")).isEqualTo(expectedQuery);
                    assertThat(ops.get("variables"))
                            .asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsExactlyInAnyOrderEntriesOf(expectedVars);
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testDecodesMap() {
        final Mono<Map<String, Set<ObjectGraphPath>>> actual = mpGqlJsonDecoder.decodePart(
                new MockFormFieldPart(GraphQlMultipartPartKey.MAP.getKeyName(), GqlTestData.getTestFileMapJson()),
                GraphQlMultipartPartKey.MAP.getTypeRef());

        final Map<String, Set<ObjectGraphPath>> expectedMap = new JsonMapper().readValue(
                GqlTestData.getTestFileMapJson(), new TypeReference<>() { });

        StepVerifier.create(actual)
                .assertNext(map -> {
                    assertThat(map.keySet()).containsExactlyInAnyOrderElementsOf(expectedMap.keySet());
                    assertThat(map).containsExactlyInAnyOrderEntriesOf(expectedMap);
                })
                .expectComplete()
                .verify();
    }
}
