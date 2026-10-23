package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import graphql.com.google.common.collect.Maps;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <h2>DecodingUtils</h2>
 * <p>
 * Utilities used while decoding multipart GraphQL requests.
 * <ul>
 * <li>Validates decoded {@link ObjectGraphPath} values target variables, with an operation index for batches.</li>
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
     * Check an indexed path is rooted at an operation's variables.
     * @param path decoded batch destination
     * @return whether the path has an operation index, variables prefix, and non-empty segments
     */
    protected static boolean isValidBatchPath(final ObjectGraphPath path)
    {
        return path != null && path.isValid() && !path.key().isEmpty()
                && path.pathSegments().size() >= 2
                && path.pathSegments().get(0).matches("0|[1-9][0-9]*")
                && REQUIRED_PATH_PREFIX.equals(path.pathSegments().get(1))
                && path.pathSegments().stream().noneMatch(String::isEmpty);
    }

    /**
     * Check that a path identifies an existing null placeholder without creating graph nodes.
     * @param root decoded operations root
     * @param path destination to validate
     * @return whether all path segments exist and the leaf is null
     */
    protected static boolean isNullFilePlaceholder(final Object root, final ObjectGraphPath path)
    {
        final List<String> segments = new ArrayList<>(path.pathSegments());
        segments.add(path.key());
        Object current = root;
        for (final String segment : segments)
        {
            if (current instanceof Map<?, ?> map && map.containsKey(segment))
            {
                current = map.get(segment);
            }
            else if (current instanceof List<?> list && segment.matches("0|[1-9][0-9]*"))
            {
                try
                {
                    final int index = Integer.parseInt(segment);
                    if (index >= list.size())
                    {
                        return false;
                    }
                    current = list.get(index);
                }
                catch (NumberFormatException exception)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return current == null;
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
