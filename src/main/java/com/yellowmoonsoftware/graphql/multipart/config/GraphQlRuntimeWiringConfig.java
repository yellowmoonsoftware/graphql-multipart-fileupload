package com.yellowmoonsoftware.graphql.multipart.config;

import com.yellowmoonsoftware.graphql.multipart.scalars.UploadScalar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.reactive.GraphQlWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * <h2>GraphQlRuntimeWiringConfig</h2>
 * Autoconfiguration that registers the `Upload` scalar with GraphQL runtime wiring.
 * <ul>
 * <li>Runs before Spring GraphQL auto-config to ensure the scalar is available.</li>
 * <li>Conditional on {@link RuntimeWiringConfigurer} presence and absence of existing bean.</li>
 * </ul>
 */
@AutoConfiguration
@AutoConfigureBefore({GraphQlAutoConfiguration.class, GraphQlWebMvcAutoConfiguration.class, GraphQlWebFluxAutoConfiguration.class})
@ConditionalOnClass(RuntimeWiringConfigurer.class)
public class GraphQlRuntimeWiringConfig {

    /**
     * Provide a {@link RuntimeWiringConfigurer} to register the `Upload` scalar when missing.
     * @return runtime wiring configurer that adds the Upload scalar
     */
    @Bean
    @ConditionalOnMissingBean(name = "graphQlUploadScalarConfigurer")
    public RuntimeWiringConfigurer graphQlUploadScalarConfigurer() {
        return builder -> builder.scalar(UploadScalar.INSTANCE);
    }
}
