package com.yellowmoonsoftware.graphql.multipart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlMultipartRequestTest {

    @SuppressWarnings("unchecked")
    @Test
    void buildRequestExtractsOperationComponentsAndAppliesVariableReplacement() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rawOperationsMap = objectMapper.readValue(GqlTestData.getTestOperationsJson(true, true), new TypeReference<>() { });

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
