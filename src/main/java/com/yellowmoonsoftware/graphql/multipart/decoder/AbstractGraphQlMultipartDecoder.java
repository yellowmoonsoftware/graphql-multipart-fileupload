package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartRequest;
import com.yellowmoonsoftware.graphql.multipart.util.MapUtils;
import com.yellowmoonsoftware.graphql.multipart.util.MapListGraphTraverser;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import graphql.com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

/**
 * <h2>AbstractGraphQlMultipartDecoder</h2>
 * Base implementation of {@link GraphQlMultipartDecoder} that wires together JSON decoding
 * of the `operations` and `map` parts, joins file parts, and injects files into variables.
 * <p>
 * Implementations supply `decodePart` to turn a single multipart part into a typed map.
 */
@Slf4j
public abstract class AbstractGraphQlMultipartDecoder implements GraphQlMultipartDecoder {

    /**
     * Protected constructor to allow subclassing
     */
    protected AbstractGraphQlMultipartDecoder() { }

    /**
     * Join the file map to available file parts and produce mapped file entries for valid paths.
     * @param filePathMap file key to a set of object graph paths
     * @param partsMap multipart parts keyed by the form field name
     * @return stream of valid mapped files
     */
    protected Stream<GraphQlMappedFile> decodeFileMap(final Map<String, Set<ObjectGraphPath>> filePathMap, final Map<String, Part> partsMap) {
        return MapUtils.joinToStream(filePathMap, DecodingUtils.filterFileParts(partsMap))
                .flatMap(e -> e.flatMapLeft(Set::stream))
                .filter(e -> DecodingUtils.isValidPath(e.leftValue()))
                .map(e -> new GraphQlMappedFile(e.rightValue(), e.leftValue()))
                .peek(mf -> log.trace("Decoded multipart file: {} -> {}", mf.path(), mf.file().filename()));
    }

    /**
     * Decode a single multipart part into a typed map (operations or map).
     * @param part multipart part to decode
     * @param typeRef target map type
     * @param <T> value type in the map
     * @return mono containing decoded map
     */
    protected abstract <T> Mono<Map<String,T>> decodePart(@NonNull final Part part, @NonNull final ParameterizedTypeReference<Map<String, T>> typeRef);

    /**
     * Decode a multipart part by key name, defaulting to an empty map when absent.
     * @param partsMap all parts keyed by name
     * @param key enum describing which part to decode
     * @param <T> value type in the decoded map
     * @return mono containing decoded map or empty map if missing
     */
    protected <T> Mono<Map<String,T>> decodePart(final Map<String, Part> partsMap, final GraphQlMultipartPartKey key) {
        return Optional.ofNullable(partsMap.get(key.getKeyName()))
                .map(part -> decodePart(part, key.<T>getTypeRef()))
                .orElseGet(() -> {
                    log.warn("No multipart part named {} found; using empty map instead.", key.getKeyName());
                    return Mono.just(Maps.newHashMap());
                });
    }

    /**
     * Decode the multipart payload into a [GraphQlRequest], injecting files into variables.
     * @param multipartData multipart form data keyed by part name
     * @return mono emitting the built [GraphQlRequest]
     */
    @Override
    public Mono<GraphQlRequest> decode(final MultiValueMap<String, Part> multipartData) {
        final Map<String, Part> partsMap = multipartData.toSingleValueMap();

        final Mono<Map<String, Set<ObjectGraphPath>>> pathMap = this.decodePart(partsMap, GraphQlMultipartPartKey.MAP);
        final Mono<Map<String, Object>> gqlRawOps = this.decodePart(partsMap, GraphQlMultipartPartKey.OPERATIONS);

        return gqlRawOps.zipWith(pathMap, (ops, m) -> {
            log.trace("Decoded multipart operations and path map.  Ops Keys: [{}]  Path Map Keys: [{}]",
                    String.join(", ", ops.keySet()),
                    String.join(", ", m.keySet()));
            decodeFileMap(m, partsMap)
                    .forEach(mf -> MapListGraphTraverser.wrap(ops).set(mf.path(), mf.file()));

            return GraphQlMultipartRequest.build(ops);
        });
    }

}
