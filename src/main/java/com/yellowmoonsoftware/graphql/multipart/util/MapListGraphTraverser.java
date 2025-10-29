package com.yellowmoonsoftware.graphql.multipart.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <h2>MapListGraphTraverser</h2>
 * Generic {@link ObjectGraphTraverser} for nested {@link Map}/{@link List} graphs using string paths.
 * @param <TKey> key type used to access elements in underlying {@link Map} ({@link String}) or {@link List} ({@link Integer}).
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MapListGraphTraverser<TKey> implements ObjectGraphTraverser {
    private final Function<String, TKey> keyConvert;
    private final Function<TKey, Boolean> hasKey;
    private final Function<TKey, Object> extractor;
    private final BiFunction<TKey, Object, Object> mutator;

    /**
     * Set a value on the underlying collection using a string key.
     * @param key string key/index
     * @param value value to set
     * @param <T> value type
     * @return value set or null if key invalid
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T set(final String key, final T value) {
        final TKey typedKey = tryKeyAccess(key);
        if (typedKey != null) {
            return (T)mutator.apply(typedKey, value);
        }
        return null;
    }

    /**
     * Get a value from the underlying collection using a string key.
     * @param key string key/index
     * @param <T> expected type
     * @return value or null if missing/invalid key
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        final TKey typedKey = tryKeyAccess(key);
        if (typedKey != null) {
            return (T)extractor.apply(typedKey);
        }
        return null;
    }

    /**
     * Set a value using a multi-segment {@link ObjectGraphPath}, traversing maps/lists as needed.
     * @param path parsed path
     * @param value value to set
     * @param <T> value type
     * @return value set or null if path invalid
     */
    @Override
    public <T> T set(final ObjectGraphPath path, T value) {
        log.trace("Setting value [{}] at path: {}", value, path);
        return path.pathSegments()
                    .stream()
                    .reduce(this,
                            ObjectGraphTraverser::dereference,
                            ExtendedBinaryOperator.firstArg())
                    .set(path.key(), value);
    }

    /**
     * Get a value using a multi-segment {@link ObjectGraphPath}, traversing maps/lists as needed.
     * @param path parsed path
     * @param <T> expected type
     * @return value or null if missing
     */
    @Override
    public <T> T get(final ObjectGraphPath path) {
        return path.pathSegments()
                .stream()
                .reduce(this,
                        ObjectGraphTraverser::dereference,
                        ExtendedBinaryOperator.firstArg())
                .get(path.key());
    }

    /**
     * Dereference a nested node by string key and wrap it as a traverser.
     * @param key string key/index
     * @return traverser for nested value or null-object
     */
    public ObjectGraphTraverser dereference(final String key) {
        return MapListGraphTraverser.wrap(this.get(key));
    }

    /**
     * Attempt to convert and validate a string key for access.
     * @param path string path segment
     * @return typed key when valid, otherwise null
     */
    protected TKey tryKeyAccess(final String path) {
        try {
            TKey key = keyConvert.apply(path);
            return hasKey.apply(key) ? key : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Wrap an object into a traverser: Map/List supported, otherwise null-object.
     * @param obj object to wrap
     * @return traverser for Map/List or {@link NullObjectGraphTraverser} otherwise
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ObjectGraphTraverser wrap(final Object obj) {
        if (obj instanceof List l) {
            return new MapListGraphTraverser<>(Integer::parseInt, i -> i < l.size() && i >= 0, l::get, (i, v) -> l.set(i, v));
        }

        if (obj instanceof Map m) {
            return new MapListGraphTraverser<>(Function.identity(), m::containsKey, m::get, (k, v) -> m.put(k, v));
        }

        return NullObjectGraphTraverser.INSTANCE;
    }
}
