package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartPayload;
import com.yellowmoonsoftware.graphql.multipart.MockFilePart;
import com.yellowmoonsoftware.graphql.multipart.MockFormFieldPart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphQlMultipartBatchDecoderTest
{
    private final GraphQlMultipartJsonDecoder decoder = new GraphQlMultipartJsonDecoder(new JacksonJsonDecoder());
    private final MockFilePart file = new MockFilePart("shared.txt", "upload", "shared content");
    private static final String OPERATIONS = """
            [
              {"query":"mutation First($file: Upload!) { upload(file: $file) }",
               "operationName":"First", "extensions":{"trace":"first"}, "variables":{"file":null}},
              {"query":"mutation Second($input: Input!) { nested(input: $input) }",
               "operationName":"Second", "variables":{"input":{"files":[null,null]}}},
              {"query":"{ noop }"}
            ]
            """;

    @Test
    void mapsOneFileToMultipleOperationsAndNestedListEntries()
    {
        final MultiValueMap<String, Part> parts = parts(OPERATIONS, """
                {"upload":["0.variables.file","1.variables.input.files.0","1.variables.input.files.1"]}
                """);

        StepVerifier.create(decoder.decodePayload(parts))
                .assertNext(payload -> {
                    assertThat(payload.batched()).isTrue();
                    assertThat(payload.requests()).hasSize(3);
                    assertThat(payload.requests().get(0).getOperationName()).isEqualTo("First");
                    assertThat(payload.requests().get(0).getExtensions()).containsEntry("trace", "first");
                    assertThat(payload.requests().get(0).getVariables().get("file")).isSameAs(file);
                    final Map<?, ?> input = (Map<?, ?>) payload.requests().get(1).getVariables().get("input");
                    final List<?> files = (List<?>) input.get("files");
                    assertThat(files.get(0)).isSameAs(file);
                    assertThat(files.get(1)).isSameAs(file);
                    assertThat(payload.requests().get(2).getVariables()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void retainsArrayShapeForOneOperationAndAnEmptyFileMap()
    {
        StepVerifier.create(decoder.decodePayload(parts("[{\"query\":\"{ noop }\"}]", "{}")))
                .assertNext(payload -> {
                    assertThat(payload.batched()).isTrue();
                    assertThat(payload.requests()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void preservesSingleOperationDecodingAndLenientMapping()
    {
        final MultiValueMap<String, Part> parts = parts(
                "{\"query\":\"{ noop }\",\"variables\":{\"file\":null}}",
                "{\"missing\":[\"variables.file\"],\"upload\":[\"extensions.file\"]}");
        StepVerifier.create(decoder.decodePayload(parts))
                .assertNext(payload -> {
                    assertThat(payload.batched()).isFalse();
                    assertThat(payload.requests().get(0).getVariables()).containsEntry("file", null);
                })
                .verifyComplete();
        parts.remove("map");
        StepVerifier.create(decoder.decode(parts)).expectNextCount(1).verifyComplete();
    }

    @Test
    void legacySingleOperationMethodRejectsEvenAOneOperationArray()
    {
        StepVerifier.create(decoder.decode(parts("[{\"query\":\"{ noop }\"}]", "{}")))
                .expectError(ServerWebInputException.class).verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "null", "42", "\"query\"", "[null]", "[[]]", "[{}]",
            "[{\"query\":42}]", "[{\"query\":\" \"}]",
            "[{\"query\":\"{ noop }\",\"variables\":[]}]",
            "[{\"query\":\"{ noop }\",\"extensions\":42}]",
            "[{\"query\":\"{ noop }\",\"operationName\":42}]", "["})
    void rejectsInvalidOperationShapes(final String operations)
    {
        StepVerifier.create(decoder.decodePayload(parts(operations, "{}")))
                .expectError(ServerWebInputException.class).verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"variables.file", "-1.variables.file", "3.variables.file", "+1.variables.file",
            "01.variables.file", "999999999999999999999.variables.file", "0.extensions.file",
            "0.variables.missing", "0.variables.file.child", "0.variables.file.",
            "1.variables.input.files.2", "1.variables.input.files.-1", "1.variables.input.files.00",
            "1.variables.input.files.foo", "1.variables.input.files.999999999999999999999",
            "1.variables..files.0", "0.variables", ""})
    void rejectsInvalidBatchDestinations(final String path)
    {
        StepVerifier.create(decoder.decodePayload(parts(OPERATIONS, "{\"upload\":[\"" + path + "\"]}")))
                .expectError(ServerWebInputException.class).verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"absent\":[\"0.variables.file\"]}", "{\"upload\":null}",
            "{\"upload\":[]}", "{\"upload\":[null]}", "{\"upload\":42}", "[]", "null", "{"})
    void rejectsInvalidFileMaps(final String map)
    {
        StepVerifier.create(decoder.decodePayload(parts(OPERATIONS, map)))
                .expectError(ServerWebInputException.class).verify();
    }

    @Test
    void rejectsConflictingFilesAndNonNullPlaceholders()
    {
        final MultiValueMap<String, Part> parts = parts(OPERATIONS,
                "{\"upload\":[\"0.variables.file\"],\"other\":[\"0.variables.file\"]}");
        parts.add("other", new MockFilePart("other.txt", "other", "other content"));
        StepVerifier.create(decoder.decodePayload(parts)).expectError(ServerWebInputException.class).verify();
        StepVerifier.create(decoder.decodePayload(parts(
                        "[{\"query\":\"{ noop }\",\"variables\":{\"file\":\"occupied\"}}]",
                        "{\"upload\":[\"0.variables.file\"]}")))
                .expectError(ServerWebInputException.class).verify();
    }

    @Test
    void rejectsMissingRequiredPartsForBatches()
    {
        final MultiValueMap<String, Part> parts = parts(OPERATIONS, "{}");
        parts.remove("map");
        StepVerifier.create(decoder.decodePayload(parts)).expectError(ServerWebInputException.class).verify();
        parts.remove("operations");
        StepVerifier.create(decoder.decodePayload(parts)).expectError(ServerWebInputException.class).verify();
    }

    @Test
    void payloadEnforcesCardinalityAndAnImmutableRequestList()
    {
        assertThatThrownBy(() -> new GraphQlMultipartPayload(List.of(), true))
                .isInstanceOf(IllegalArgumentException.class);
        StepVerifier.create(decoder.decodePayload(parts(OPERATIONS, "{}")))
                .assertNext(payload -> {
                    assertThatThrownBy(() -> new GraphQlMultipartPayload(payload.requests(), false))
                            .isInstanceOf(IllegalArgumentException.class);
                    assertThatThrownBy(() -> payload.requests().clear()).isInstanceOf(UnsupportedOperationException.class);
                }).verifyComplete();
    }

    private MultiValueMap<String, Part> parts(final String operations, final String map)
    {
        final MultiValueMap<String, Part> parts = new LinkedMultiValueMap<>();
        parts.add("operations", new MockFormFieldPart("operations", operations));
        parts.add("map", new MockFormFieldPart("map", map));
        parts.add("upload", file);
        return parts;
    }
}
