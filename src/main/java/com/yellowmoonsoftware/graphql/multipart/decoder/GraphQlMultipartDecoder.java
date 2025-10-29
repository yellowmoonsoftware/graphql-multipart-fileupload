package com.yellowmoonsoftware.graphql.multipart.decoder;

import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

/**
 * <h2>GraphQlMultipartDecoder</h2>
 * <p>
 * Contract for converting a multipart GraphQL upload request into a {@link GraphQlRequest}.</p>
 * <p>
 * Implementations should be non-blocking and return results via Reactor {@link Mono}.</p>
 */
public interface GraphQlMultipartDecoder {
    /**
     * Decode multipart form-data that follows the GraphQL multipart request spec.
     * @param multipartData aggregated multipart parts keyed by form field name (e.g., `operations`, `map`, file keys)
     * @return a {@link Mono} emitting the fully constructed {@link GraphQlRequest} with files bound into variables
     */
    Mono<GraphQlRequest> decode(final MultiValueMap<String, Part> multipartData);
}
