package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;
import java.util.Set;

/**
 * <h2>GraphQlMultipartPartKey</h2>
 * <p>
 * Enum representing keys used in decoding multipart GraphQL requests.
 * </p>
 * <p>
 * This enum is designed to facilitate handling multipart GraphQL payloads consisting of
 * an `operations` part and a `map` part, adhering to GraphQL multipart request specifications.
 * </p>
 * <p>
 * Each key in this enum is associated with:
 * </p>
 * <ul>
 * <li>A specific part name in the multipart request payload.
 * </li>
 * <li>A {@link ParameterizedTypeReference} to facilitate deserialization into the expected data
 * structure during request decoding.</li>
 * </ul>
 */
@RequiredArgsConstructor
public enum GraphQlMultipartPartKey {
    /**
     * Used for identifying and decoding the "operations" part of the payload, which typically contains GraphQL
     * query details, variables, and extensions.
     */
    OPERATIONS("operations", new ParameterizedTypeReference<Map<String, Object>>() { }),
    /**
     * Used for identifying and decoding the "map" part of the payload, used for describing file mapping metadata.
     */
    MAP("map", new ParameterizedTypeReference<Map<String, Set<ObjectGraphPath>>>() { });

    @Getter
    private final String keyName;

    private final ParameterizedTypeReference<?> typeRef;

    /**
     * Get the type reference associated with this multipart part key.
     * @param <T> map value type
     * @return typed {@link ParameterizedTypeReference} for decoding
     */
    @SuppressWarnings("unchecked")
    public <T> ParameterizedTypeReference<Map<String, T>> getTypeRef() {
        return (ParameterizedTypeReference<Map<String, T>>) this.typeRef;
    }
}
