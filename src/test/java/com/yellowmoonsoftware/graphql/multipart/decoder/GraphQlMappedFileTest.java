package com.yellowmoonsoftware.graphql.multipart.decoder;

import com.yellowmoonsoftware.graphql.multipart.MockFilePart;
import com.yellowmoonsoftware.graphql.multipart.util.ObjectGraphPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMappedFileTest {

    @Test
    void exposesFileAndPath() {
        final FilePart filePart = new MockFilePart("avatar.png", "avatar", "data");
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.avatar");

        final GraphQlMappedFile mappedFile = new GraphQlMappedFile(filePart, path);

        assertThat(mappedFile.file()).isSameAs(filePart);
        assertThat(mappedFile.path()).isEqualTo(path);
    }

    @Test
    void equalityIncludesFileAndPath() {
        final FilePart filePart = new MockFilePart("avatar.png", "avatar", "data");
        final FilePart otherFilePart = new MockFilePart("resume.pdf", "resume", "pdf-data");
        final ObjectGraphPath path = ObjectGraphPath.from("variables.user.avatar");
        final ObjectGraphPath otherPath = ObjectGraphPath.from("variables.user.resume");

        final GraphQlMappedFile first = new GraphQlMappedFile(filePart, path);
        final GraphQlMappedFile same = new GraphQlMappedFile(filePart, path);
        final GraphQlMappedFile differentPath = new GraphQlMappedFile(filePart, otherPath);
        final GraphQlMappedFile differentFile = new GraphQlMappedFile(otherFilePart, path);

        assertThat(first).isEqualTo(same);
        assertThat(first).hasSameHashCodeAs(same);
        assertThat(first).isNotEqualTo(differentPath);
        assertThat(first).isNotEqualTo(differentFile);
    }
}
