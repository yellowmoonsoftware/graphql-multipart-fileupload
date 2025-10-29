package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.jayway.jsonpath.*;
import com.yellowmoonsoftware.graphql.multipart.GqlTestData;
import com.yellowmoonsoftware.graphql.multipart.JsonPathTestConfig;
import com.yellowmoonsoftware.graphql.multipart.MockFormFieldPart;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartJsonDecoderTest {
    Jackson2JsonDecoder decoder;

    GraphQlMultipartJsonDecoder mpGqlJsonDecoder;

    @BeforeAll
    static void fixtureSetup() {
        JsonPathTestConfig.integrateJackson();
    }

    @BeforeEach
    void setup() {
        decoder = new Jackson2JsonDecoder();
        mpGqlJsonDecoder = new GraphQlMultipartJsonDecoder(decoder);
    }

    @Test
    void testDecodesOperations() {
        final Mono<Map<String, Object>> actual = mpGqlJsonDecoder.decodePart(
                new MockFormFieldPart(GraphQlMultipartPartKey.OPERATIONS.getKeyName(), GqlTestData.getTestOperationsJson()),
                GraphQlMultipartPartKey.OPERATIONS.getTypeRef());

        final DocumentContext opsDocCtx = JsonPath.parse(GqlTestData.getTestOperationsJson());
        final String expectedQuery = opsDocCtx.read("$.query", String.class);
        final Map<String, Object> expectedVars = opsDocCtx.read("$.variables", new TypeRef<>() { });

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

        final DocumentContext mapDocCtx = JsonPath.parse(GqlTestData.getTestFileMapJson());
        final Map<String, Set<ObjectGraphPath>> expectedMap = mapDocCtx.read("$", new TypeRef<>() { });

        StepVerifier.create(actual)
                .assertNext(map -> {
                    assertThat(map.keySet()).containsExactlyInAnyOrderElementsOf(expectedMap.keySet());
                    assertThat(map).containsExactlyInAnyOrderEntriesOf(expectedMap);
                })
                .expectComplete()
                .verify();
    }
}