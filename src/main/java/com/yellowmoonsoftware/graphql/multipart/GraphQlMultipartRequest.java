package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.util.MapAccessor;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.support.DefaultGraphQlRequest;

import java.util.Map;

/**
 * <h2>GraphQlMultipartRequest</h2>
 * Wrapper around {@link DefaultGraphQlRequest} built from decoded multipart `operations`.
 */
@Getter
public class GraphQlMultipartRequest extends DefaultGraphQlRequest {
    private final static ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF = new ParameterizedTypeReference<>() {};
    private final static ParameterizedTypeReference<String> STRING_TYPE_REF = new ParameterizedTypeReference<>() {};

    private GraphQlMultipartRequest(final String document, final String operationName, final Map<String, Object> variables, final Map<String, Object> extensions) {
        super(document, operationName, variables, extensions);
    }

    /**
     * Build a request from decoded `operations` content.
     * @param operations decoded operations map
     * @return constructed {@link GraphQlMultipartRequest}
     */
    public static GraphQlMultipartRequest build(final Map<String, Object> operations) {
        final MapAccessor<String> ops = MapAccessor.wrap(operations);
        return new GraphQlMultipartRequest(
                ops.get(QUERY_KEY, STRING_TYPE_REF),
                ops.get(OPERATION_NAME_KEY, STRING_TYPE_REF),
                ops.get(VARIABLES_KEY, MAP_TYPE_REF),
                ops.get(EXTENSIONS_KEY, MAP_TYPE_REF)
        );
    }
}
