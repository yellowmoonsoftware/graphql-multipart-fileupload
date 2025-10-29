package com.yellowmoonsoftware.graphql.multipart.util;

import lombok.extern.slf4j.Slf4j;

/**
 * <h2>NullObjectGraphTraverser</h2>
 * <p>
 * A final singleton implementation of {@link ObjectGraphTraverser} that performs no operations.
 * <p>
 * - `set` and `get` always return `null`.
 * <p>
 * - `dereference` always returns the same singleton instance.
 * <p>
 * - Equality and hashCode are defined such that only instances of this class are equal.
 * <p>
 * Constructor is package-private to enforce the singleton.
 */
@Slf4j
public final class NullObjectGraphTraverser implements ObjectGraphTraverser {

    /**
     * Singleton instance of the null-object traverser.
     */
    public static final NullObjectGraphTraverser INSTANCE = new NullObjectGraphTraverser();

    NullObjectGraphTraverser() { }

    /**
     * No-op set for the specified key; always returns null.
     * @param key path segment
     * @param value value to set
     * @param <T> value type
     * @return always null
     */
    @Override
    public <T> T set(final String key, T value) {
        log.warn("Attempted to set value [{}] for key [{}] on NullObjectGraphTraverser.", value, key);
        return null;
    }

    /**
     * No-op get for the specified key; always returns null.
     * @param key path segment
     * @param <T> expected type
     * @return always null
     */
    @Override
    public <T> T get(final String key) {
        log.warn("Attempted to get value for key [{}] on NullObjectGraphTraverser.", key);
        return null;
    }

    /**
     * No-op set for {@link ObjectGraphPath}; always returns null.
     * @param path object graph path
     * @param value value to set
     * @param <T> value type
     * @return always null
     */
    @Override
    public <T> T set(final ObjectGraphPath path, T value) {
        log.warn("Attempted to set value [{}] at path [{}] on NullObjectGraphTraverser.", value, path);
        return null;
    }

    /**
     * No-op get for {@link ObjectGraphPath}; always returns null.
     * @param path object graph path
     * @param <T> expected type
     * @return always null
     */
    @Override
    public <T> T get(final ObjectGraphPath path) {
        log.warn("Attempted to get value at path [{}] on NullObjectGraphTraverser.", path);
        return null;
    }

    /**
     * Always returns the singleton instance.
     * @param key path segment to dereference
     * @return singleton {@link NullObjectGraphTraverser}
     */
    @Override
    public ObjectGraphTraverser dereference(final String key) {
        log.warn("Attempted to dereference key [{}] on NullObjectGraphTraverser.", key);
        return NullObjectGraphTraverser.INSTANCE;
    }

    /**
     * Equality is based solely on type (all instances are equal to the singleton).
     * @param obj the other object to compare
     * @return true when the other object is also NullObjectGraphTraverser
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NullObjectGraphTraverser;
    }

    /**
     * Hash code derived from the class to align with equals semantics.
     * @return class-based hash code
     */
    @Override
    public int hashCode() {
        return NullObjectGraphTraverser.class.hashCode();
    }
}
