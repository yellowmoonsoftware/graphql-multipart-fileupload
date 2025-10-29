package com.yellowmoonsoftware.graphql.multipart.config;

import com.yellowmoonsoftware.graphql.multipart.scalars.UploadScalar;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQlRuntimeWiringConfigTest {

    @Mock
    RuntimeWiring.Builder mockBuilder;

    @Captor
    ArgumentCaptor<GraphQLScalarType> scalarTypeCaptor;

    private final GraphQlRuntimeWiringConfig config = new GraphQlRuntimeWiringConfig();

    @Test
    void createsRuntimeWiringConfigurer() {
        final RuntimeWiringConfigurer configurer = config.graphQlUploadScalarConfigurer();
        assertThat(configurer).isNotNull();
    }

    @Test
    void configurerSetsUploadScaler() {
        final RuntimeWiringConfigurer configurer = config.graphQlUploadScalarConfigurer();

        lenient().when(mockBuilder.scalar(scalarTypeCaptor.capture()))
                .thenReturn(mockBuilder);
        configurer.configure(mockBuilder);

        assertThat(scalarTypeCaptor.getValue()).isEqualTo(UploadScalar.INSTANCE);
    }
}