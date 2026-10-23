package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartPayload;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * <h2>GraphQlMultipartDecoder</h2>
 * <p>
 * Contract for decoding multipart GraphQL uploads as a single request or an ordered batch.</p>
 * <p>
 * Implementations should be non-blocking and return results via Reactor {@link Mono}.</p>
 */
public interface GraphQlMultipartDecoder {
    /**
     * Decode multipart form-data containing a single operations object.
     * @param multipartData aggregated multipart parts keyed by form field name (e.g., `operations`, `map`, file keys)
     * @return a {@link Mono} emitting the fully constructed {@link GraphQlRequest} with files bound into variables
     * @see #decodePayload(MultiValueMap)
     */
    Mono<GraphQlRequest> decode(final MultiValueMap<String, Part> multipartData);

    /**
     * Decode a single operation or a batch, preserving the original operations shape.
     * The default implementation adapts existing single-operation decoders.
     * @param multipartData aggregated multipart parts keyed by form field name
     * @return decoded payload with files bound into variables
     * @see #decode(MultiValueMap)
     */
    default Mono<GraphQlMultipartPayload> decodePayload(final MultiValueMap<String, Part> multipartData)
    {
        return decode(multipartData).map(request -> new GraphQlMultipartPayload(List.of(request), false));
    }
}
