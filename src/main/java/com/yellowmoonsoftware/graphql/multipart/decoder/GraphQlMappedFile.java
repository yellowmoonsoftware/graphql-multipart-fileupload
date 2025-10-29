package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.springframework.http.codec.multipart.FilePart;

/**
 * <h2>GraphQlMappedFile</h2>
 * <p>
 * Pairing of an uploaded {@link FilePart} with the GraphQL variable, specified by a dotted-path, it should populate.
 * </p>
 * <p>
 * Instances are produced while decoding the multipart "map" section and later applied to the
 * decoded operations to place each file into the appropriate location in the GraphQl variables map.
 * </p>
 * @param file the uploaded file content
 * @param path the target variable path (e.g., `variables.user.avatar`)
 */
public record GraphQlMappedFile(FilePart file, ObjectGraphPath path) { }
