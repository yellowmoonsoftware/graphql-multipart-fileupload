package com.yellowmoonsoftware.graphql.multipart.decoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * <h2>GraphQlMultipartJsonDecoder</h2>
 * <p>
 * Default JSON-backed implementation of {@link AbstractGraphQlMultipartDecoder}.
 * </p>
 * <ul>
 * <li>Uses a Spring {@link Decoder} (typically `Jackson2JsonDecoder`) to read the `operations` and `map` parts of a
 * GraphQL multipart request.</li>
 * <li>Produces typed maps that the base class combines to attach files to variables and build the final GraphQL
 * request.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class GraphQlMultipartJsonDecoder extends AbstractGraphQlMultipartDecoder {
    private final Decoder<?> jsonDecoder;

    /**
     * Decode the multipart part into a {@link Map} of {@link String} to specified type {@link T} using the configured JSON decoder.
     * @param part    multipart part (`operations` or `map`)
     * @param typeRef target map type reference
     * @param <T>     value type of the decoded map
     * @return {@link Mono} emitting the decoded map
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<Map<String,T>> decodePart(@NonNull final Part part, @NonNull final ParameterizedTypeReference<Map<String, T>> typeRef) {
        log.trace("Decoding part {} as JSON into {}", part.name(), typeRef.getType());
        final Decoder<Map<String,T>> typedDecoder = (Decoder<Map<String,T>>) jsonDecoder;
        return typedDecoder.decodeToMono(part.content(), ResolvableType.forType(typeRef), MediaType.APPLICATION_JSON, null);
    }
}
