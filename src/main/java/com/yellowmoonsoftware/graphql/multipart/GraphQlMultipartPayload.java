package com.yellowmoonsoftware.graphql.multipart;

import org.springframework.graphql.GraphQlRequest;

import java.util.List;

/**
 * <h2>GraphQlMultipartPayload</h2>
 * Decoded operations, retaining whether the multipart request contained an array.
 * @param requests operations in their original order
 * @param batched whether the operations were encoded as an array, including a one-element array
 */
public record GraphQlMultipartPayload(List<GraphQlRequest> requests, boolean batched)
{
    /**
     * Copy the request list and validate the payload cardinality.
     * @param requests decoded operations
     * @param batched whether the operations were encoded as an array
     */
    public GraphQlMultipartPayload
    {
        requests = List.copyOf(requests);
        if (requests.isEmpty() || (!batched && requests.size() != 1))
        {
            throw new IllegalArgumentException("A payload requires one request or a non-empty batch");
        }
    }
}
