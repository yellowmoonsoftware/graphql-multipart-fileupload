package com.yellowmoonsoftware.graphql.multipart.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import graphql.com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * <h2>ObjectGraphPath</h2>
 * Immutable representation of a dotted path split into segments plus leaf key.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ObjectGraphPath {
    private static final Pattern PERIOD = Pattern.compile("\\.");

    /**
     * Leaf key of the path.
     */
    @EqualsAndHashCode.Include
    private final String key;
    /**
     * Path segments leading to the leaf key.
     */
    @EqualsAndHashCode.Include
    private final List<String> pathSegments;

    /**
     * Flag indicating whether the path is valid (key is non-null).
     */
    private final boolean isValid;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final String toStringCache = buildToStringCache();

    private String buildToStringCache() {
        final String segmentPath = String.join(".", this.pathSegments());
        return "$" +
                segmentPath +
                "[" +
                (key() != null ? key() : "") +
                "] " +
                (this.isValid() ? "VALID" : "INVALID");
    }

    /**
     * Protected constructor to allow controlled creation with pre-split segments and key.
     * @param key leaf key
     * @param pathSegments path segments leading to the key
     */
    protected ObjectGraphPath(final String key, @NonNull final List<String> pathSegments) {
        this.key = key;
        this.pathSegments = Collections.unmodifiableList(pathSegments);
        this.isValid = this.key != null;
    }

    /**
     * Parse a dotted string path into an {@link ObjectGraphPath}, splitting segments and leaf key.
     * @param path dotted path string
     * @return parsed {@link ObjectGraphPath}
     */
    @JsonCreator()
    public static ObjectGraphPath from(final String path) {
        final List<String> segments = Optional.ofNullable(path)
                .filter(p -> !p.isEmpty())
                .map(p -> Lists.newArrayList(PERIOD.split(p)))
                .orElse(Lists.newArrayList());

        final String key = !segments.isEmpty() ? segments.remove(segments.size() - 1) : null;

        return new ObjectGraphPath(key, Collections.unmodifiableList(segments));
    }

    /**
     * Return the string representation of this {@link ObjectGraphPath}, e.g. "variables.user\[name\] \[VALID\]"
     */
    @Override
    public String toString() {
        return toStringCache();
    }
}
