package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
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

    private static final int BATCH_CONCURRENCY = 8;

    /**
     * Supported media types for single-operation responses; batches use application/json.
     */
    public static final List<MediaType> SUPPORTED_MEDIA_TYPES =
            List.of(APPLICATION_GRAPHQL_RESPONSE, MediaType.APPLICATION_JSON);

    private final GraphQlMultipartDecoder graphQlMultipartDecoder;
    private final WebGraphQlHandler webGraphQlHandler;

    /**
     * Decode multipart data and delegate each operation to {@link WebGraphQlHandler}.
     * Batches execute concurrently with their results rendered in input order.
     * @param serverRequest incoming multipart server request
     * @return mono producing the HTTP response
     */
    public Mono<ServerResponse> handleGraphQlMultipartRequest(final ServerRequest serverRequest)
    {
        log.trace("Handling multipart GraphQL request: {}", serverRequest.uri());
        return serverRequest.multipartData()
                .flatMap(graphQlMultipartDecoder::decodePayload)
                .flatMap(payload -> {
                    final String requestId = serverRequest.exchange().getRequest().getId();
                    if (!payload.batched())
                    {
                        return execute(serverRequest, payload.requests().get(0), requestId)
                                .flatMap(response -> buildServerResponse(response, serverRequest.headers().accept()));
                    }
                    if (!acceptsBatchResponse(serverRequest.headers().accept()))
                    {
                        return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).build();
                    }
                    return Flux.range(0, payload.requests().size())
                            .flatMapSequential(index -> execute(serverRequest, payload.requests().get(index), requestId + ":" + index),
                                    BATCH_CONCURRENCY)
                            .collectList()
                            .flatMap(GraphQlMultipartWebHandler::buildBatchResponse);
                });
    }

    private Mono<WebGraphQlResponse> execute(final ServerRequest serverRequest,
                                            final GraphQlRequest request, final String id)
    {
        final WebGraphQlRequest graphQlRequest = new WebGraphQlRequest(serverRequest.uri(),
                serverRequest.headers().asHttpHeaders(), serverRequest.cookies(),
                serverRequest.remoteAddress().orElse(null), serverRequest.attributes(), request, id,
                serverRequest.exchange().getLocaleContext().getLocale());
        log.debug("Dispatching decoded GraphQL request: {}", graphQlRequest);
        return webGraphQlHandler.handleRequest(graphQlRequest);
    }

    private static boolean acceptsBatchResponse(final List<MediaType> acceptableMediaTypes)
    {
        // A specific application/json;q=0 takes precedence over a positive wildcard.
        return acceptableMediaTypes.isEmpty() || acceptableMediaTypes.stream()
                .filter(type -> type.isCompatibleWith(MediaType.APPLICATION_JSON))
                .sorted(Comparator.comparingInt((MediaType type) ->
                        type.isWildcardType() ? 0 : type.isWildcardSubtype() ? 1 : 2).reversed())
                .findFirst()
                .map(type -> type.getQualityValue() > 0)
                .orElse(false);
    }

    private static Mono<ServerResponse> buildBatchResponse(final List<WebGraphQlResponse> responses)
    {
        final HttpHeaders mergedHeaders = new HttpHeaders();
        responses.forEach(response -> response.getResponseHeaders().forEach((name, values) -> {
            // Framing and content type belong to the combined response.
            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)
                    || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                    || HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(name))
            {
                return;
            }
            if (HttpHeaders.SET_COOKIE.equalsIgnoreCase(name))
            {
                mergedHeaders.addAll(name, values);
            }
            else if (!mergedHeaders.containsHeader(name))
            {
                mergedHeaders.addAll(name, values);
            }
        }));
        return ServerResponse.ok()
                .headers(headers -> headers.putAll(mergedHeaders))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(responses.stream().map(WebGraphQlResponse::toMap).toList());
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
