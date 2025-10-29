package com.yellowmoonsoftware.graphql.multipart.scalars;

import com.yellowmoonsoftware.graphql.multipart.MockFilePart;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.NullValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;

import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadScalarTest {

    private final UploadScalar scalar = new UploadScalar();
    private final GraphQLContext context = GraphQLContext.newContext().build();
    private final Locale locale = Locale.US;

    @Test
    void parseValueReturnsFilePart() {
        final MockFilePart filePart = new MockFilePart("sample.txt", "file", "sample");

        final FilePart parsed = scalar.parseValue(filePart, context, locale);

        assertThat(parsed).isSameAs(filePart);
    }

    @Test
    void parseValueRejectsNonFilePart() {
        assertThatThrownBy(() -> scalar.parseValue("not-a-file", context, locale))
                .isInstanceOf(CoercingParseValueException.class)
                .hasMessageContaining(String.class.getName());
    }

    @Test
    void serializeAlwaysFails() {
        assertThatThrownBy(() -> scalar.serialize(new Object(), context, locale))
                .isInstanceOf(CoercingSerializeException.class)
                .hasMessageContaining("input-only");
    }

    @Test
    void parseLiteralAlwaysFails() {
        final NullValue nullLiteral = NullValue.newNullValue().build();

        assertThatThrownBy(() -> scalar.parseLiteral(nullLiteral, CoercedVariables.of(Collections.emptyMap()), context, locale))
                .isInstanceOf(CoercingParseLiteralException.class)
                .hasMessageContaining("input-only");
    }
}
