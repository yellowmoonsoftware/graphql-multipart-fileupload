package com.yellowmoonsoftware.graphql.multipart;

import com.yellowmoonsoftware.graphql.multipart.decoder.GraphQlMultipartDecoder;
import graphql.ExecutionInput;
import graphql.ExecutionResultImpl;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.support.DefaultExecutionGraphQlResponse;
import org.springframework.http.codec.multipart.Part;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.server.EntityResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphQlMultipartBatchExecutionTest
{
    @Test
    void runsAtMostEightOperationsConcurrentlyAndRetainsInputOrder()
    {
        final List<GraphQlRequest> operations = IntStream.range(0, 10)
                .mapToObj(index -> (GraphQlRequest) GraphQlMultipartRequest.build(
                        Map.of("query", "{ noop }", "variables", Map.of("index", index))))
                .toList();
        final List<Sinks.One<WebGraphQlResponse>> completions = IntStream.range(0, 10)
                .mapToObj(index -> Sinks.<WebGraphQlResponse>one()).toList();
        final AtomicInteger active = new AtomicInteger();
        final AtomicInteger started = new AtomicInteger();
        final List<Integer> completionOrder = new ArrayList<>();
        final GraphQlMultipartDecoder decoder = mock(GraphQlMultipartDecoder.class);
        when(decoder.decodePayload(any())).thenReturn(Mono.just(new GraphQlMultipartPayload(operations, true)));
        final WebGraphQlHandler execution = mock(WebGraphQlHandler.class);
        when(execution.handleRequest(any())).thenAnswer(invocation -> {
            final WebGraphQlRequest request = invocation.getArgument(0);
            final int index = (Integer) request.getVariables().get("index");
            return completions.get(index).asMono()
                    .doOnSubscribe(subscription -> {
                        active.incrementAndGet();
                        started.incrementAndGet();
                    })
                    .doOnNext(response -> completionOrder.add(index))
                    .doOnTerminate(active::decrementAndGet);
        });
        final MockServerRequest request = MockServerRequest.builder()
                .exchange(MockServerWebExchange.from(MockServerHttpRequest.post("/graphql")))
                .body(Mono.just(new LinkedMultiValueMap<String, Part>()));

        StepVerifier.create(new GraphQlMultipartWebHandler(decoder, execution).handleGraphQlMultipartRequest(request))
                .then(() -> {
                    assertThat(active).hasValue(8);
                    assertThat(started).hasValue(8);
                    for (int index = 7; index >= 1; index--)
                    {
                        completions.get(index).tryEmitValue(response(index));
                    }
                    assertThat(active).hasValue(1);
                    completions.get(0).tryEmitValue(response(0));
                    assertThat(started).hasValue(10);
                    assertThat(active).hasValue(2);
                    completions.get(9).tryEmitValue(response(9));
                    completions.get(8).tryEmitValue(response(8));
                })
                .assertNext(response -> {
                    assertThat(response).isInstanceOf(EntityResponse.class);
                    final Object body = ((EntityResponse<?>) response).entity();
                    assertThat(body).isEqualTo(IntStream.range(0, 10)
                            .mapToObj(index -> Map.of("data", Map.of("index", index))).toList());
                })
                .expectComplete().verify(Duration.ofSeconds(5));
        assertThat(completionOrder).containsExactly(7, 6, 5, 4, 3, 2, 1, 0, 9, 8);
        assertThat(active).hasValue(0);
    }

    private static WebGraphQlResponse response(final int index)
    {
        return new WebGraphQlResponse(new DefaultExecutionGraphQlResponse(
                ExecutionInput.newExecutionInput().query("{ noop }").build(),
                ExecutionResultImpl.newExecutionResult().data(Map.of("index", index)).build()));
    }
}
