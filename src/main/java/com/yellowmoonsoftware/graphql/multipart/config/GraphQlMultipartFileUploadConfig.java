package com.yellowmoonsoftware.graphql.multipart.config;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartJsonDecoder;
import com.yellowmoonsoftware.graphql.multipart.GraphQlMultipartWebHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.graphql.autoconfigure.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

/**
 * <h2>GraphQlMultipartFileUploadConfig</h2>
 * Autoconfiguration wiring for handling multipart GraphQL file uploads with Spring WebGraphQL.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(WebGraphQlHandler.class)
public class GraphQlMultipartFileUploadConfig {

    /**
     * Provide a {@link GraphQlMultipartDecoder} backed by {@link JacksonJsonDecoder}.
     * @param jsonDecoderProvider provider for JacksonJsonDecoder
     * @param jsonMapper JSON mapper fallback
     * @return configured multipart decoder
     */
    @Bean
    @ConditionalOnMissingBean
    public GraphQlMultipartDecoder graphQlMultipartDecoder(final ObjectProvider<JacksonJsonDecoder> jsonDecoderProvider,
                                                           final JsonMapper jsonMapper) {
        final JacksonJsonDecoder jsonDecoder = jsonDecoderProvider.getIfAvailable(() -> new JacksonJsonDecoder(jsonMapper));
        log.info("Configured GraphQlMultipartDecoder using JacksonJsonDecoder for multipart GraphQL decoding.");
        return new GraphQlMultipartJsonDecoder(jsonDecoder);
    }

    /**
     * Provide the {@link GraphQlMultipartWebHandler} that delegates to the core {@link WebGraphQlHandler}.
     * @param graphQlMultipartDecoder decoder for multipart GraphQL requests
     * @param webGraphQlHandler core GraphQL handler
     * @return multipart web handler
     */
    @Bean
    @ConditionalOnMissingBean
    public GraphQlMultipartWebHandler graphQlMultipartWebHandler(final GraphQlMultipartDecoder graphQlMultipartDecoder,
                                                                 final WebGraphQlHandler webGraphQlHandler) {
        log.info("Configured GraphQlMultipartWebHandler for multipart GraphQL request handling.");
        return new GraphQlMultipartWebHandler(graphQlMultipartDecoder, webGraphQlHandler);
    }

    /**
     * Route multipart GraphQL POSTs to the multipart handler when no custom router is defined.
     * @param properties GraphQL properties (includes http path)
     * @param graphQlMultipartWebHandler multipart handler
     * @return router function for multipart GraphQL requests
     */
    @Bean
    @ConditionalOnMissingBean(name = "graphQlMultipartRouter")
    @Order(-10)
    public RouterFunction<ServerResponse> graphQlMultipartRouter(final GraphQlProperties properties,
                                                                 final GraphQlMultipartWebHandler graphQlMultipartWebHandler) {
        log.info("Configured default multipart GraphQL router for POST requests on {} with content type {}, accepting {}.",
                properties.getHttp().getPath(),
                MULTIPART_FORM_DATA,
                GraphQlMultipartWebHandler.SUPPORTED_MEDIA_TYPES);
        return RouterFunctions.route()
                .POST(properties.getHttp().getPath(),
                        RequestPredicates
                                .contentType(MULTIPART_FORM_DATA)
                                .and(RequestPredicates.accept(GraphQlMultipartWebHandler.SUPPORTED_MEDIA_TYPES.toArray(new MediaType[]{}))),
                        graphQlMultipartWebHandler::handleGraphQlMultipartRequest)
                .build();
    }
}
