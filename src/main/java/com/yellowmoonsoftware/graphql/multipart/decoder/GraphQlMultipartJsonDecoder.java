package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartPayload;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * <h2>GraphQlMultipartJsonDecoder</h2>
 * <p>
 * Default JSON-backed implementation of {@link AbstractGraphQlMultipartDecoder}.
 * </p>
 * <ul>
 * <li>Uses a Spring {@link Decoder} (typically `JacksonJsonDecoder`) to read the `operations` and `map` parts of a
 * GraphQL multipart request.</li>
 * <li>Preserves operations objects or arrays while the base class attaches files to variables and builds
 * the individual GraphQL requests.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class GraphQlMultipartJsonDecoder extends AbstractGraphQlMultipartDecoder {
    private final Decoder<?> jsonDecoder;

    /**
     * Decode one operation, rejecting arrays rather than silently discarding batch entries.
     * @param multipartData aggregated multipart parts
     * @return single decoded request
     * @see #decodePayload(MultiValueMap)
     */
    @Override
    public Mono<GraphQlRequest> decode(final MultiValueMap<String, Part> multipartData)
    {
        return decodePayload(multipartData).map(payload -> {
            if (payload.batched())
            {
                throw new ServerWebInputException("Use decodePayload to decode an operations array");
            }
            return payload.requests().get(0);
        });
    }

    /**
     * Decode operations as an object or array, then inject files using paths from the map part.
     * @param multipartData aggregated multipart parts
     * @return decoded operations retaining their original shape
     */
    @Override
    public Mono<GraphQlMultipartPayload> decodePayload(final MultiValueMap<String, Part> multipartData)
    {
        return Mono.defer(() -> {
            final Map<String, Part> parts = multipartData.toSingleValueMap();
            final Part operations = parts.get(GraphQlMultipartPartKey.OPERATIONS.getKeyName());
            if (operations == null)
            {
                return Mono.error(new ServerWebInputException("Missing 'operations' part"));
            }
            return jsonDecoder.decodeToMono(operations.content(), ResolvableType.forClass(Object.class),
                            MediaType.APPLICATION_JSON, null)
                    .cast(Object.class)
                    .switchIfEmpty(Mono.error(new ServerWebInputException("Empty 'operations' part")))
                    .zipWith(this.<Set<ObjectGraphPath>>decodePart(
                                    parts, GraphQlMultipartPartKey.MAP)
                                    .switchIfEmpty(Mono.error(new ServerWebInputException("Empty 'map' part"))),
                            (root, paths) -> buildPayload(root, paths, parts));
        }).onErrorMap(DecodingException.class,
                exception -> new ServerWebInputException("Invalid multipart JSON", null, exception));
    }

    /**
     * Decode the multipart part into a {@link Map} of {@link String} to specified type {@link T} using the configured JSON decoder.
     * @param part    multipart part (`operations` or `map`)
     * @param typeRef target map type reference
     * @param <T>     value type of the decoded map
     * @return {@link Mono} emitting the decoded map
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<Map<String,T>> decodePart(@NonNull final Part part, @NonNull final ParameterizedTypeReference<Map<String, T>> typeRef) {
        log.trace("Decoding part {} as JSON into {}", part.name(), typeRef.getType());
        final Decoder<Map<String,T>> typedDecoder = (Decoder<Map<String,T>>) jsonDecoder;
        return typedDecoder.decodeToMono(part.content(), ResolvableType.forType(typeRef), MediaType.APPLICATION_JSON, null);
    }
}
