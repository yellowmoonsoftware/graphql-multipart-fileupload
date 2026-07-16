package com.yellowmoonsoftware.graphql.multipart;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartRequestTest {

    @SuppressWarnings("unchecked")
    @Test
    void buildRequestExtractsOperationComponentsAndAppliesVariableReplacement() throws JacksonException {
        final JsonMapper jsonMapper = new JsonMapper();
        Map<String, Object> rawOperationsMap = jsonMapper.readValue(GqlTestData.getTestOperationsJson(true, true), new TypeReference<>() { });

        final String expectedDocument = (String)rawOperationsMap.get("query");
        final String expectedOperationName = (String)rawOperationsMap.get("operationName");
        final Map<String, Object> expectedExtensions = (Map<String, Object>) rawOperationsMap.get("extensions");
        final Map<String, Object> expectedVariables = (Map<String, Object>) rawOperationsMap.get("variables");

        final GraphQlMultipartRequest request = GraphQlMultipartRequest.build(rawOperationsMap);

        assertThat(request.getDocument()).isEqualTo(expectedDocument);
        assertThat(request.getOperationName()).isEqualTo(expectedOperationName);
        assertThat(request.getExtensions()).isEqualTo(expectedExtensions);
        assertThat(request.getVariables()).isEqualTo(expectedVariables);
    }

}
