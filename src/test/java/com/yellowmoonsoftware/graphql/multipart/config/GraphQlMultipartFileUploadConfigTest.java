package com.yellowmoonsoftware.graphql.multipart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartJsonDecoder;
import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartWebHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphQlMultipartFileUploadConfigTest {

    private final GraphQlMultipartFileUploadConfig config = new GraphQlMultipartFileUploadConfig();

    @Mock
    ObjectProvider<Jackson2JsonDecoder> decoderProvider;

    ObjectMapper objectMapper;

    @Mock
    GraphQlMultipartDecoder multipartDecoder;

    @Mock
    WebGraphQlHandler webGraphQlHandler;

    @Mock
    GraphQlMultipartWebHandler webHandler;

    @Mock
    Jackson2JsonDecoder jacksonDecoder;

    @BeforeEach
    void resetMocks() {
        objectMapper = new ObjectMapper();
        reset(decoderProvider, multipartDecoder, webGraphQlHandler, webHandler, jacksonDecoder);
    }

    @Test
    void createsMultipartDecoderUsingExistingDecoder() {
        when(decoderProvider.getIfAvailable(any())).thenReturn(jacksonDecoder);

        final GraphQlMultipartDecoder decoder = config.graphQlMultipartDecoder(decoderProvider, objectMapper);

        assertThat(decoder).isInstanceOf(GraphQlMultipartJsonDecoder.class);
    }

    @Test
    void createsMultipartDecoderUsingFallback() {
        when(decoderProvider.getIfAvailable(any())).thenAnswer(invocation -> {
            final java.util.function.Supplier<Jackson2JsonDecoder> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        final GraphQlMultipartDecoder decoder = config.graphQlMultipartDecoder(decoderProvider, objectMapper);

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
