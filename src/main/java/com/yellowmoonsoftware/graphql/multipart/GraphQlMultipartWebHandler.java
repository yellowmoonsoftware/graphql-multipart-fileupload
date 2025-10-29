package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_GRAPHQL_RESPONSE;

/**
 * <h2>GraphQlMultipartWebHandler</h2>
 * Handles multipart GraphQL requests by decoding uploads and delegating to {@link WebGraphQlHandler}.
 */
    @Slf4j
    @RequiredArgsConstructor
    public class GraphQlMultipartWebHandler {

        /**
         * Supported media types for GraphQL responses produced by this handler.
         */
        @SuppressWarnings("removal")
        public static final List<MediaType> SUPPORTED_MEDIA_TYPES =
                Arrays.asList(APPLICATION_GRAPHQL_RESPONSE, MediaType.APPLICATION_JSON, MediaType.APPLICATION_GRAPHQL);

        private final GraphQlMultipartDecoder graphQlMultipartDecoder;
        private final WebGraphQlHandler webGraphQlHandler;

    /**
     * Decode multipart data into a GraphQL request, delegate to WebGraphQlHandler, and render the response.
     * @param serverRequest incoming multipart server request
     * @return mono producing the HTTP response
     */
    public Mono<ServerResponse> handleGraphQlMultipartRequest(final ServerRequest serverRequest) {
        log.trace("Handling multipart GraphQL request: {}", serverRequest.uri());
        return serverRequest.multipartData()
                .flatMap(graphQlMultipartDecoder::decode)
                .flatMap(gqlReq -> {
                    final WebGraphQlRequest graphQlRequest = new WebGraphQlRequest(serverRequest.uri(),
                            serverRequest.headers().asHttpHeaders(),
                            serverRequest.cookies(),
                            serverRequest.remoteAddress().orElse(null),
                            serverRequest.attributes(),
                            gqlReq,
                            serverRequest.exchange().getRequest().getId(),
                            serverRequest.exchange().getLocaleContext().getLocale());

                    log.debug("Dispatching decoded GraphQL request: {}", graphQlRequest);

                    return webGraphQlHandler.handleRequest(graphQlRequest);
                })
                .flatMap(response -> buildServerResponse(response, serverRequest.headers().accept()));
    }

    /**
     * Build an HTTP response from a {@link WebGraphQlResponse}, honoring acceptable media types.
     * @param response GraphQL response to render
     * @param acceptableMediaTypes media types accepted by the client
     * @return server response with selected content type
     */
    protected static Mono<ServerResponse> buildServerResponse(final WebGraphQlResponse response, final List<MediaType> acceptableMediaTypes) {
        final MediaType responseContentType = Optional.ofNullable(acceptableMediaTypes)
                .flatMap(t -> t.stream()
                        .filter(SUPPORTED_MEDIA_TYPES::contains)
                        .findFirst())
                .orElse(MediaType.APPLICATION_JSON);

        return ServerResponse.ok()
                .headers(headers -> headers.putAll(response.getResponseHeaders()))
                .contentType(responseContentType)
                .bodyValue(response.toMap());
    }
}
