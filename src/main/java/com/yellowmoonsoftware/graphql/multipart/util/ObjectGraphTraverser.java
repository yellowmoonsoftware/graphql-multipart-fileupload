package com.yellowmoonsoftware.graphql.multipart.util;

/**
 * <h2>ObjectGraphTraverser</h2>
 * Interface for traversing and manipulating object graphs. Provides methods
 * to access and modify properties in a nested structure using string-based paths.
 */
public interface ObjectGraphTraverser {
    /**
     * Set a value at the given string key.
     * @param key path segment
     * @param value value to set
     * @param <T> value type
     * @return the value set or null if not applied
     */
    <T> T set(String key, T value);
    /**
     * Get a value at the given string key.
     * @param key path segment
     * @param <T> expected type
     * @return value or null if missing
     */
    <T> T get(String key);
    /**
     * Traverse and set a value at the location in this graph defined by an {@link ObjectGraphPath}.
     * @param path parsed object graph path
     * @param value value to set
     * @param <T> value type
     * @return the value set or null if not applied
     */
    <T> T set(ObjectGraphPath path, T value);
    /**
     * Traverse and get a value at the location in this graph defined by an {@link ObjectGraphPath}.
     * @param path parsed object graph path
     * @param <T> expected type
     * @return value or null if missing
     */
    <T> T get(ObjectGraphPath path);
    /**
     * Dereference a nested map/list node by key and return a traverser.
     * @param key path segment to dereference
     * @return traverser for the nested value or a null-object traverser
     */
    ObjectGraphTraverser dereference(String key);
}
