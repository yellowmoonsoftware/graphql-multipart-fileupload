package com.yellowmoonsoftware.graphql.multipart.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.NonNull;

import java.util.Locale;

/**
 * <h2>UploadScalar</h2>
 * Custom GraphQL scalar for handling file uploads (input-only, expects {@link FilePart}).
 * <ul>
 * <li>
 * Validates input during value parsing.
 * </li>
 * <li>
 * Disallows serialization and literal parsing.
 * </li>
 * </ul>
 */
public class UploadScalar implements Coercing<FilePart, Void> {

    /**
     * Reusable Upload scalar instance for schema registration.
     */
    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("Upload")
            .coercing(new UploadScalar())
            .build();

    /**
     * Upload cannot be serialized (input-only); always throws.
     * @param dataFetcherResult value to serialize (ignored)
     * @param graphQLContext context
     * @param locale locale
     * @return never returns; always throws
     */
    @Override
    public Void serialize(@NonNull Object dataFetcherResult, @NonNull GraphQLContext graphQLContext, @NonNull Locale locale) throws CoercingSerializeException {
        throw new CoercingSerializeException("Upload is an input-only type and cannot be serialized");
    }

    /**
     * Validate and extract {@link FilePart} during variable parsing.
     * @param input input value
     * @param graphQLContext context
     * @param locale locale
     * @return parsed {@link FilePart}
     */
    @Override
    public FilePart parseValue(@NonNull final Object input,
                               @NonNull final GraphQLContext graphQLContext,
                               @NonNull final Locale locale) throws CoercingParseValueException {
        if (input instanceof FilePart fileInput) {
            return fileInput;
        }
        throw new CoercingParseValueException("Expected type FilePart but was " + input.getClass().getName());
    }

    /**
     * Literal parsing is not supported for Upload (input-only); always throws.
     * @param input literal value
     * @param variables coerced variables
     * @param graphQLContext context
     * @param locale locale
     * @return never returns; always throws
     */
    @Override
    public FilePart parseLiteral(@NonNull final Value<?> input,
                                 @NonNull final CoercedVariables variables,
                                 @NonNull final GraphQLContext graphQLContext,
                                 @NonNull final Locale locale) throws CoercingParseLiteralException {
        throw new CoercingParseLiteralException("Upload is an input-only type and cannot be parsed from literals");
    }
}
