package com.yellowmoonsoftware.graphql.multipart;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import java.util.Set;

public class JsonPathTestConfig {
     public static void integrateJackson() {
        final Set<Option> defaultOptions = Configuration.defaultConfiguration().getOptions();

        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public Set<Option> options() {
                return defaultOptions;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }
        });
    }
}
