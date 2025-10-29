package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.MockFilePart;
import com.yellowmoonsoftware.graphql.multipart.MockFormFieldPart;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractGraphQlMultipartDecoderTest {

    @Spy
    private AbstractGraphQlMultipartDecoder decoder;

    @SuppressWarnings("unchecked")
    @Test
    void decodeInvokesDecodePartForPresentParts() {
        final MockFormFieldPart operationsPart = new MockFormFieldPart("operations", "{}");
        final MockFormFieldPart mapPart = new MockFormFieldPart("map", "{}");
        final MockFilePart filePart = new MockFilePart("avatar.png", "0", "data");

        final MultiValueMap<String, Part> multipart = new MultiValueMapAdapter<>(Map.of(
                "operations", List.of(operationsPart),
                "map", List.of(mapPart),
                "0", List.of(filePart)
        ));

        final Map<String, Object> operations = new HashMap<>();
        final Map<String, Object> variables = new HashMap<>();
        final Map<String, Object> user = new HashMap<>();
        user.put("avatar", null);
        variables.put("user", user);
        operations.put("query", "mutation upload");
        operations.put("variables", variables);

        final Map<String, Set<ObjectGraphPath>> pathMap = Map.of(
                "0", Set.of(ObjectGraphPath.from("variables.user.avatar"))
        );

        doAnswer(invocation -> {
            final ParameterizedTypeReference<?> typeRef = invocation.getArgument(1);
            if (GraphQlMultipartPartKey.OPERATIONS.getTypeRef().equals(typeRef)) {
                return Mono.just(operations);
            }
            if (GraphQlMultipartPartKey.MAP.getTypeRef().equals(typeRef)) {
                return Mono.just(pathMap);
            }
            return Mono.empty();
        }).when(decoder).decodePart(any(Part.class), any(ParameterizedTypeReference.class));

        final Mono<GraphQlRequest> result = decoder.decode(multipart);

        StepVerifier.create(result)
                .assertNext(request -> {
                    assertThat(request.getDocument()).isEqualTo("mutation upload");
                    assertThat(request.getVariables())
                            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                            .extractingByKey("user")
                            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                            .containsEntry("avatar", filePart);
                })
                .expectComplete()
                .verify();

        verify(decoder, times(1))
                .decodePart(eq(operationsPart), eq(GraphQlMultipartPartKey.OPERATIONS.getTypeRef()));
        verify(decoder, times(1))
                .decodePart(eq(mapPart), eq(GraphQlMultipartPartKey.MAP.getTypeRef()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void decodeSkipsMissingParts() {
        final MockFormFieldPart operationsPart = new MockFormFieldPart("operations", "{}");
        final MockFilePart filePart = new MockFilePart("avatar.png", "0", "data");

        final MultiValueMap<String, Part> multipart = new MultiValueMapAdapter<>(Map.of(
                "operations", List.of(operationsPart),
                "0", List.of(filePart)
        ));

        final Map<String, Object> operations = new HashMap<>();
        final Map<String, Object> variables = new HashMap<>();
        final Map<String, Object> user = new HashMap<>();
        user.put("avatar", null);
        variables.put("user", user);
        operations.put("query", "mutation upload");
        operations.put("variables", variables);

        doAnswer(invocation -> {
            final ParameterizedTypeReference<?> typeRef = invocation.getArgument(1);
            if (GraphQlMultipartPartKey.OPERATIONS.getTypeRef().equals(typeRef)) {
                return Mono.just(operations);
            }
            return Mono.empty();
        }).when(decoder).decodePart(any(Part.class), any(ParameterizedTypeReference.class));

        final Mono<GraphQlRequest> result = decoder.decode(multipart);

        StepVerifier.create(result)
                .assertNext(request -> assertThat(request.getVariables())
                        .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                        .extractingByKey("user")
                        .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                        .containsEntry("avatar", null))
                .expectComplete()
                .verify();

        verify(decoder, times(1))
                .decodePart(eq(operationsPart), eq(GraphQlMultipartPartKey.OPERATIONS.getTypeRef()));
        verify(decoder, never())
                .decodePart(any(), eq(GraphQlMultipartPartKey.MAP.getTypeRef()));
    }
}
