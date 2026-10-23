package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartPayload;
import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartRequest;
import com.yellowmoonsoftware.graphql.multipart.util.MapListGraphTraverser;
import com.yellowmoonsoftware.graphql.multipart.util.MapUtils;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import graphql.com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
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
            return buildPayload(ops, m, partsMap).requests().get(0);
        });
    }

    /**
     * Inject files into the original operations root and construct ordered requests.
     * Single-operation mapping retains its existing lenient handling of invalid paths.
     * @param operations decoded operations object or array
     * @param filePathMap file field names and destination paths
     * @param partsMap multipart parts keyed by field name
     * @return payload retaining the original object or array shape
     */
    @SuppressWarnings("unchecked")
    protected GraphQlMultipartPayload buildPayload(final Object operations,
                                                  final Map<String, Set<ObjectGraphPath>> filePathMap,
                                                  final Map<String, Part> partsMap)
    {
        if (operations instanceof Map<?, ?> map)
        {
            decodeFileMap(filePathMap, partsMap)
                    .forEach(file -> MapListGraphTraverser.wrap(map).set(file.path(), file.file()));
            return new GraphQlMultipartPayload(List.of(GraphQlMultipartRequest.build((Map<String, Object>) map)), false);
        }
        if (!(operations instanceof List<?> batch) || batch.isEmpty())
        {
            throw new ServerWebInputException("'operations' must be an object or a non-empty array of objects");
        }
        if (!partsMap.containsKey(GraphQlMultipartPartKey.MAP.getKeyName()))
        {
            throw new ServerWebInputException("A batch requires a 'map' part (use {} when no files are mapped)");
        }
        // Validate every operation and mapping before building or executing any request.
        batch.forEach(AbstractGraphQlMultipartDecoder::validateBatchOperation);
        final List<GraphQlMappedFile> files = new ArrayList<>();
        final Set<ObjectGraphPath> destinations = new HashSet<>();
        filePathMap.forEach((name, paths) -> {
            if (!(partsMap.get(name) instanceof FilePart file))
            {
                throw new ServerWebInputException("Missing file part for batch map entry '" + name + "'");
            }
            if (paths == null || paths.isEmpty())
            {
                throw new ServerWebInputException("Batch map entry '" + name + "' requires destination paths");
            }
            paths.forEach(path -> {
                if (!DecodingUtils.isValidBatchPath(path)
                        || !DecodingUtils.isNullFilePlaceholder(batch, path)
                        || !destinations.add(path))
                {
                    throw new ServerWebInputException("Invalid or conflicting batch file path: " + path);
                }
                files.add(new GraphQlMappedFile(file, path));
            });
        });
        files.forEach(file -> MapListGraphTraverser.wrap(batch).set(file.path(), file.file()));
        final List<GraphQlRequest> requests = batch.stream()
                .map(operation -> (GraphQlRequest) GraphQlMultipartRequest.build((Map<String, Object>) operation))
                .toList();
        return new GraphQlMultipartPayload(requests, true);
    }

    private static void validateBatchOperation(final Object operation)
    {
        if (!(operation instanceof Map<?, ?> map)
                || !(map.get("query") instanceof String query) || !StringUtils.hasText(query)
                || (map.get("operationName") != null && !(map.get("operationName") instanceof String))
                || (map.get("variables") != null && !(map.get("variables") instanceof Map))
                || (map.get("extensions") != null && !(map.get("extensions") instanceof Map)))
        {
            throw new ServerWebInputException("Each batch operation requires a query string and valid request fields");
        }
    }
}
