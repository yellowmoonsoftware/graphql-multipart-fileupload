package com.yellowmoonsoftware.graphql.multipart;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public record MockFilePart(String filename, String name, HttpHeaders headers, DataBuffer fileContent) implements FilePart {


    public MockFilePart(final String filename, final String name, final String fileContent) {
        this(filename, name, fileContent.getBytes());
    }

    public MockFilePart(final String filename, final String name, final byte[] fileContent) {
        this(filename, name, new HttpHeaders(), DefaultDataBufferFactory.sharedInstance.wrap(fileContent));
        this.headers.setContentType(MediaType.TEXT_PLAIN);
    }

    @Override
    @NonNull
    public Mono<Void> transferTo(@NonNull final Path dest) {
        return Mono.empty();
    }

    @Override
    @NonNull
    public Flux<DataBuffer> content() {
        return Flux.defer(() -> Flux.just(fileContent));
    }
}
