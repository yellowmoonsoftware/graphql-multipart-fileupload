package com.yellowmoonsoftware.graphql.multipart.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * <h2>MapAccessor</h2>
 * Type-safe accessor wrapper around a {@link Map}, providing optional defaults and runtime type checks.
 * @param <T> key type
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MapAccessor<T> {
    @Getter
    private final Map<T, ?> map;

    /**
     * Get a value by key when it matches the expected type; otherwise return null.
     * @param key map key
     * @param typeRef expected type reference
     * @param <U> expected return type
     * @return value when type matches, or null
     */
    public <U> U get(@NonNull T key, final ParameterizedTypeReference<U> typeRef) {
        return get(key, () -> null, typeRef);
    }

    /**
     * Get a value by key, falling back to a supplier when the value is absent or of the wrong type.
     * @param key map key
     * @param defaultValueSupplier supplier invoked when missing/wrong type
     * @param typeRef expected type reference
     * @param <U> expected return type
     * @return value when type matches, supplier-provided default otherwise
     */
    @SuppressWarnings("unchecked")
    public <U> U get(@NonNull T key, @NonNull final Supplier<? extends U> defaultValueSupplier, final ParameterizedTypeReference<U> typeRef) {
        final Object val = map.get(key);
        if (val == null && map.containsKey(key)) {
            return null;
        }

        final ResolvableType resolvable = ResolvableType.forType(typeRef.getType());
        final Class<?> raw = resolvable.resolve();

        return raw != null && raw.isInstance(val)
                ? (U)raw.cast(val)
                : defaultValueSupplier.get();
    }

    /**
     * Wrap a map to get typed access helpers.
     * @param map underlying map
     * @param <U> key type
     * @return {@link MapAccessor} for the provided map
     */
    public static <U> MapAccessor<U> wrap(@NonNull Map<U, ?> map) {
        return new MapAccessor<>(map);
    }
}
