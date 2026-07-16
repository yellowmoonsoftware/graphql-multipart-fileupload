package com.yellowmoonsoftware.graphql.multipart.config;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartJsonDecoder;
import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartWebHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.graphql.autoconfigure.GraphQlProperties;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphQlMultipartFileUploadConfigTest {

    private final GraphQlMultipartFileUploadConfig config = new GraphQlMultipartFileUploadConfig();

    @Mock
    ObjectProvider<JacksonJsonDecoder> decoderProvider;

    JsonMapper jsonMapper;

    @Mock
    GraphQlMultipartDecoder multipartDecoder;

    @Mock
    WebGraphQlHandler webGraphQlHandler;

    @Mock
    GraphQlMultipartWebHandler webHandler;

    @Mock
    JacksonJsonDecoder jacksonDecoder;

    @BeforeEach
    void resetMocks() {
        jsonMapper = new JsonMapper();
        reset(decoderProvider, multipartDecoder, webGraphQlHandler, webHandler, jacksonDecoder);
    }

    @Test
    void createsMultipartDecoderUsingExistingDecoder() {
        when(decoderProvider.getIfAvailable(any())).thenReturn(jacksonDecoder);

        final GraphQlMultipartDecoder decoder = config.graphQlMultipartDecoder(decoderProvider, jsonMapper);

        assertThat(decoder).isInstanceOf(GraphQlMultipartJsonDecoder.class);
    }

    @Test
    void createsMultipartDecoderUsingFallback() {
        when(decoderProvider.getIfAvailable(any())).thenAnswer(invocation -> {
            final java.util.function.Supplier<JacksonJsonDecoder> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        final GraphQlMultipartDecoder decoder = config.graphQlMultipartDecoder(decoderProvider, jsonMapper);

        assertThat(decoder).isInstanceOf(GraphQlMultipartJsonDecoder.class);
    }

    @Test
    void createsWebHandler() {
        final GraphQlMultipartWebHandler handler = config.graphQlMultipartWebHandler(multipartDecoder, webGraphQlHandler);

        assertThat(handler).isNotNull();
    }

    @Test
    void createsRouterFunction() {
        final GraphQlProperties properties = new GraphQlProperties();
        properties.getHttp().setPath("/graphql");

        final RouterFunction<ServerResponse> router = config.graphQlMultipartRouter(properties, webHandler);

        assertThat(router).isNotNull();
    }
}
