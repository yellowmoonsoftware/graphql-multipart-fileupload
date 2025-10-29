package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import graphql.com.google.common.collect.Maps;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;

import java.util.Map;
import java.util.Objects;

/**
 * <h2>DecodingUtils</h2>
 * <p>
 * Utilities used while decoding multipart GraphQL requests.
 * <ul>
 * <li>Validates decoded {@link ObjectGraphPath} values start at `variables`.</li>
 * <li>Filters multipart parts down to {@link FilePart} instances for file injection.</li>
 * </ul>
 * The helpers are package-scoped to support reuse across decoder implementations without leaking
 * additional public API.
 */
public class DecodingUtils {
    private DecodingUtils() {}

    private static final String REQUIRED_PATH_PREFIX = "variables";

    /**
     * Check whether the given path is non-null, well-formed, and rooted at `variables`.
     * @param p decoded graph path from the multipart map
     * @return `true` when the path is valid and starts at `variables`
     */
    protected static boolean isValidPath(final ObjectGraphPath p) {
        return p.isValid() && !p.pathSegments().isEmpty() && Objects.equals(p.pathSegments().get(0), REQUIRED_PATH_PREFIX);
    }

    /**
     * Retain only file parts from the multipart map, dropping form fields or other part types.
     * @param partsMap multipart parts keyed by form field name
     * @return a map containing only {@link FilePart} values from the input
     */
    protected static Map<String, FilePart> filterFileParts(final Map<String, Part> partsMap) {
        return Maps.filterValues(Maps.transformValues(partsMap, p -> p instanceof FilePart fp ? fp : null), Objects::nonNull);
    }
}
