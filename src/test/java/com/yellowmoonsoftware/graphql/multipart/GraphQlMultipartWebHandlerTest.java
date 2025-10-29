package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQlMultipartWebHandlerTest {

    @Mock
    GraphQlMultipartDecoder multipartDecoder;

    @Mock
    WebGraphQlHandler webGraphQlHandler;

    @InjectMocks
    GraphQlMultipartWebHandler graphQlMultipartWebHandler;

    @Mock
    GraphQlRequest graphQlRequest;

    @Mock
    ServerRequest serverRequest;

    @Mock
    WebGraphQlResponse webGraphQlResponse;

    @Captor
    ArgumentCaptor<WebGraphQlRequest> graphQlRequestCaptor;

    @Captor
    ArgumentCaptor<MultiValueMap<String, Part>> multipartDataCaptor;

    @Test
    void handleGraphQlMultipartRequest_withValidRequest_shouldExtractMultipartDataAndReturnResponse() {
        // Arrange
        final MultiValueMap<String, Part> fakeMultipartData = new LinkedMultiValueMap<>();
        final Map<String, Object> fakeWebGqlResponseData = new HashMap<>();
        final String fakeQuery ="query { testQuery() { } }";
        final HttpHeaders fakeHeaders = new HttpHeaders();

        when(graphQlRequest.getDocument()).thenReturn(fakeQuery);

        when(serverRequest.multipartData()).thenReturn(Mono.just(fakeMultipartData));
        when(serverRequest.uri()).thenReturn(URI.create("http://localhost/graphql"));

        final ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
        when(serverRequest.headers()).thenReturn(headers);
        when(headers.asHttpHeaders()).thenReturn(fakeHeaders);
        when(headers.accept()).thenReturn(List.of(MediaType.APPLICATION_JSON));

        when(serverRequest.cookies()).thenReturn(new LinkedMultiValueMap<>());
        when(serverRequest.remoteAddress()).thenReturn(Optional.empty());
        when(serverRequest.attributes()).thenReturn(Map.of());

        final ServerWebExchange exchange = mock(ServerWebExchange.class);
        final ServerHttpRequest request = mock(ServerHttpRequest.class);
        final LocaleContext localeContext = mock(LocaleContext.class);
        when(serverRequest.exchange()).thenReturn(exchange);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn("test-request-id");
        when(exchange.getLocaleContext()).thenReturn(localeContext);
        when(localeContext.getLocale()).thenReturn(null);

        when(multipartDecoder.decode(multipartDataCaptor.capture()))
                .thenReturn(Mono.just(graphQlRequest));

        when(webGraphQlResponse.toMap()).thenReturn(fakeWebGqlResponseData);
        when(webGraphQlResponse.getResponseHeaders()).thenReturn(new HttpHeaders());

        when(webGraphQlHandler.handleRequest(graphQlRequestCaptor.capture())).thenReturn(Mono.just(webGraphQlResponse));

        // Act
        final Mono<ServerResponse> responseMono = graphQlMultipartWebHandler.handleGraphQlMultipartRequest(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                    assertThat(response)
                            .asInstanceOf(InstanceOfAssertFactories.type(EntityResponse.class))
                            .extracting(EntityResponse::entity)
                            .isEqualTo(fakeWebGqlResponseData);
                })
                .expectComplete()
                .verify();

        final WebGraphQlRequest capturedRequest = graphQlRequestCaptor.getValue();
        assertThat(capturedRequest.getUri().toUri()).isEqualTo(URI.create("http://localhost/graphql"));
        assertThat(capturedRequest.getId()).isEqualTo("test-request-id");
        assertThat(capturedRequest.getLocale()).isEqualTo(Locale.getDefault());
        assertThat(capturedRequest.getHeaders()).isEqualTo(fakeHeaders);
        assertThat(capturedRequest.toMap()).isEqualTo(Map.of("query", fakeQuery));
    }
}