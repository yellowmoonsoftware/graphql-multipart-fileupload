package com.yellowmoonsoftware.graphql.multipart;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

public record MockFormFieldPart(String value, String name, HttpHeaders headers) implements FormFieldPart {

    public MockFormFieldPart(final String name, final String value) {
        this(value, name, new HttpHeaders());
        this.headers.setContentType(MediaType.TEXT_PLAIN);
    }

    @Override
    @NonNull
    public Flux<DataBuffer> content() {
        return Flux.defer(() -> Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(this.value.getBytes())));
    }
}

