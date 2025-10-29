package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.MockFilePart;
import com.yellowmoonsoftware.graphql.multipart.MockFormFieldPart;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DecodingUtilsTest {

    @Test
    void isValidPathReturnsTrueForVariablesPrefix() {
        final ObjectGraphPath path = ObjectGraphPath.from("variables.input.file");

        assertThat(DecodingUtils.isValidPath(path)).isTrue();
    }

    @Test
    void isValidPathReturnsFalseWhenPathInvalid() {
        final ObjectGraphPath path = ObjectGraphPath.from(null);

        assertThat(DecodingUtils.isValidPath(path)).isFalse();
    }

    @Test
    void isValidPathReturnsFalseWhenPrefixDoesNotMatch() {
        final ObjectGraphPath path = ObjectGraphPath.from("query.variables.file");

        assertThat(DecodingUtils.isValidPath(path)).isFalse();
    }

    @Test
    void filterFilePartsReturnsOnlyFileParts() {
        final FilePart filePart = new MockFilePart("avatar.png", "avatar", "image-data");
        final Part formFieldPart = new MockFormFieldPart("user", "alice");
        final Map<String, Part> parts = new HashMap<>();
        parts.put("upload", filePart);
        parts.put("user", formFieldPart);

        final Map<String, FilePart> filtered = DecodingUtils.filterFileParts(parts);

        assertThat(filtered).hasSize(1);
        assertThat(filtered).containsKey("upload");
        assertThat(filtered.get("upload")).isSameAs(filePart);
    }
}
