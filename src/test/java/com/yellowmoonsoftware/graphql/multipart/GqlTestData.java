package com.yellowmoonsoftware.graphql.multipart;

public class GqlTestData {

    public static String getTestOperationsJson() {
        return getTestOperationsJson(false, false);
    }

    public static String getTestOperationsJson(final boolean includeOperationName, final boolean includeExtensions) {
        final String baseJson = """
                {
                    "query": "mutation($files: [Upload!]!) { multipleUpload(files: $files) { id } }",
                    "variables": {
                        "files": [null, null],
                        "foo": "bar"
                    }
                """
                + (includeOperationName ? """
                ,"operationName": "someMutation"
                """ : "")
                + (includeExtensions ? """
                ,"extensions": { "foo": "bar" }
                """ : "")
                +
                """
                }
                """;



        return includeOperationName ? baseJson.replace("mutation", "someMutation") : baseJson;
    }

    public static String getTestFileMapJson() {
        return getTestFileMapJson(false, false);
    }
    public static String getTestFileMapJson(final boolean withInvalidVariablePath, final boolean withInvalidFilePartRef) {
        final String filePath_0 = withInvalidVariablePath ? "\"variables.badVar\"" : "\"variables.files.0\"";

        final String filePartRef_1 = withInvalidFilePartRef ? "\"7x\"" : "\"1\"";

        return """
                { "0": [""" + filePath_0 + """
                ],""" + filePartRef_1 + """
              : ["variables.files.1"] }
              """;
    }
}
