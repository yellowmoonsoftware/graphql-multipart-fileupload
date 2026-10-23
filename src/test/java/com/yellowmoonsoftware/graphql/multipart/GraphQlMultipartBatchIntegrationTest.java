package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.config.GraphQlMultipartFileUploadConfig;
import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartJsonDecoder;
import com.yellowmoonsoftware.graphql.multipart.scalars.UploadScalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.graphql.autoconfigure.GraphQlProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.MockServerConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartBatchIntegrationTest
{
    @TempDir
    Path uploadDirectory;

    private WebTestClient client;
    private final List<WebGraphQlRequest> requests = new CopyOnWriteArrayList<>();
    private final AtomicBoolean spooledToDisk = new AtomicBoolean();
    private static final String QUERY = "mutation($file: Upload!) { singleUpload(file: $file) }";
    private static final String SINGLE = "{\"query\":\"" + QUERY + "\",\"variables\":{\"file\":null}}";

    @BeforeEach
    void setup() throws IOException
    {
        final GraphQlSource source = GraphQlSource.schemaResourceBuilder()
                .schemaResources(new ByteArrayResource("""
                        scalar Upload
                        type Query { noop: Boolean }
                        type Mutation {
                          singleUpload(file: Upload!): String!
                          multipleUpload(files: [Upload!]!): [String!]!
                        }
                        """.getBytes(StandardCharsets.UTF_8)))
                .configureRuntimeWiring(wiring -> wiring.scalar(UploadScalar.INSTANCE)
                        .type("Mutation", type -> type
                                .dataFetcher("singleUpload", environment ->
                                        readFile(environment.getArgument("file")).toFuture())
                                .dataFetcher("multipleUpload", environment ->
                                        Flux.fromIterable(environment.<List<FilePart>>getArgument("files"))
                                                .concatMap(this::readFile).collectList().toFuture())))
                .build();
        final WebGraphQlHandler execution = WebGraphQlHandler.builder(new DefaultExecutionGraphQlService(source))
                .interceptor((request, chain) -> {
                    requests.add(request);
                    return chain.next(request).doOnNext(response -> {
                        response.getResponseHeaders().add("X-Operation", request.getId());
                        response.getResponseHeaders().add(HttpHeaders.SET_COOKIE, "operation=" + request.getId());
                        if (request.getId().contains(":"))
                        {
                            response.getResponseHeaders().setContentLength(1);
                            response.getResponseHeaders().setContentType(MediaType.TEXT_PLAIN);
                        }
                    });
                }).build();
        final GraphQlMultipartWebHandler handler = new GraphQlMultipartWebHandler(
                new GraphQlMultipartJsonDecoder(new JacksonJsonDecoder()), execution);
        final GraphQlProperties properties = new GraphQlProperties();
        properties.getHttp().setPath("/uploads");
        final DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxInMemorySize(1024);
        partReader.setFileStorageDirectory(uploadDirectory);
        client = WebTestClient.bindToRouterFunction(
                        new GraphQlMultipartFileUploadConfig().graphQlMultipartRouter(properties, handler))
                .apply(new MockServerConfigurer()
                {
                    @Override
                    public void beforeServerCreated(final WebHttpHandlerBuilder builder)
                    {
                        final ServerCodecConfigurer codecs = ServerCodecConfigurer.create();
                        codecs.defaultCodecs().multipartReader(new MultipartHttpMessageReader(partReader));
                        builder.codecConfigurer(codecs);
                    }
                }).build();
    }

    @Test
    void executesTheSpecificationBatchExample()
    {
        final String operations = "[" + SINGLE + "," + """
                {"query":"mutation($files: [Upload!]!) { multipleUpload(files: $files) }",
                 "variables":{"files":[null,null]}}]
                """;
        send(operations, """
                {"0":["0.variables.file"],"1":["1.variables.files.0"],"2":["1.variables.files.1"]}
                """, "application/json", "alpha", "bravo", "charlie")
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$[0].data.singleUpload").isEqualTo("alpha")
                .jsonPath("$[1].data.multipleUpload[0]").isEqualTo("bravo")
                .jsonPath("$[1].data.multipleUpload[1]").isEqualTo("charlie");
        assertThat(requests).hasSize(2);
        assertThat(requests.get(1).getId()).isEqualTo(requests.get(0).getId().replace(":0", ":1"));
        assertThat(requests).allSatisfy(request -> {
            assertThat(request.getUri().getPath()).isEqualTo("/uploads");
            assertThat(request.getHeaders().getFirst("X-Test")).isEqualTo("context");
            assertThat(request.getCookies().getFirst("session").getValue()).isEqualTo("cookie");
            assertThat(request.getLocale().getLanguage()).isEqualTo("fr");
        });
    }

    @Test
    void rereadsSharedDiskBackedUploadAcrossOperationsAndMergesResponseHeaders()
    {
        final String content = "shared".repeat(1024);
        send("[" + SINGLE + "," + SINGLE + "]", "{\"0\":[\"0.variables.file\",\"1.variables.file\"]}",
                "application/json", content)
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().value("X-Operation", value -> assertThat(value).isEqualTo(requests.get(0).getId()))
                .expectHeader().values(HttpHeaders.SET_COOKIE, values -> assertThat(values).hasSize(2))
                .expectBody().jsonPath("$[0].data.singleUpload").isEqualTo(content)
                .jsonPath("$[1].data.singleUpload").isEqualTo(content);
        assertThat(spooledToDisk).isTrue();
    }

    @Test
    void preservesSingleResponseShapeAndGraphQlMediaType()
    {
        send(SINGLE, "{\"0\":[\"variables.file\"]}", "application/graphql-response+json", "alpha")
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_GRAPHQL_RESPONSE)
                .expectBody().jsonPath("$.data.singleUpload").isEqualTo("alpha");
    }

    @Test
    void returnsAnArrayForASingleOperationBatch()
    {
        send("[" + SINGLE + "]", "{\"0\":[\"0.variables.file\"]}", "application/json", "alpha")
                .expectStatus().isOk().expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].data.singleUpload").isEqualTo("alpha");
    }

    @Test
    void continuesAfterAnOperationReturnsGraphQlErrors()
    {
        send("[{\"query\":\"{ missingField }\"}," + SINGLE + "]",
                "{\"0\":[\"1.variables.file\"]}", "application/json", "alpha")
                .expectStatus().isOk().expectBody()
                .jsonPath("$[0].errors").isNotEmpty()
                .jsonPath("$[1].data.singleUpload").isEqualTo("alpha");
        assertThat(requests).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[{\"query\":\"{ noop }\"},null]", "[]", "["})
    void rejectsMalformedPayloadsBeforeAnyExecution(final String operations)
    {
        send(operations, "{}", "application/json").expectStatus().isBadRequest();
        assertThat(requests).isEmpty();
    }

    @Test
    void rejectsAnInvalidMappingBeforeAnyExecution()
    {
        send("[" + SINGLE + "," + SINGLE + "]", "{\"0\":[\"2.variables.file\"]}", "application/json", "alpha")
                .expectStatus().isBadRequest();
        assertThat(requests).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/graphql-response+json", "application/json;q=0, */*;q=1"})
    void rejectsUnacceptableBatchResponseTypesBeforeExecution(final String accept)
    {
        send("[" + SINGLE + "]", "{\"0\":[\"0.variables.file\"]}", accept, "alpha")
                .expectStatus().isEqualTo(406);
        assertThat(requests).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"*/*", "application/*", "application/graphql-response+json, application/json;q=0.5"})
    void acceptsJsonCompatibleBatchResponseTypes(final String accept)
    {
        send("[{\"query\":\"{ __typename }\"}]", "{}", accept)
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    private WebTestClient.ResponseSpec send(final String operations, final String map,
                                             final String accept, final String... files)
    {
        final MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("operations", operations);
        body.part("map", map);
        for (int index = 0; index < files.length; index++)
        {
            body.part(Integer.toString(index), new ByteArrayResource(files[index].getBytes(StandardCharsets.UTF_8)))
                    .filename("file" + index + ".txt").contentType(MediaType.TEXT_PLAIN);
        }
        return client.post().uri("/uploads").header(HttpHeaders.ACCEPT, accept)
                .header("X-Test", "context").cookie("session", "cookie")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "fr")
                .body(BodyInserters.fromMultipartData(body.build())).exchange();
    }

    private Mono<String> readFile(final FilePart file)
    {
        try (var files = Files.list(uploadDirectory))
        {
            spooledToDisk.compareAndSet(false, files.findAny().isPresent());
        }
        catch (IOException exception)
        {
            throw new UncheckedIOException(exception);
        }
        return DataBufferUtils.join(file.content()).map(buffer -> {
            try
            {
                return buffer.toString(StandardCharsets.UTF_8);
            }
            finally
            {
                DataBufferUtils.release(buffer);
            }
        });
    }
}
