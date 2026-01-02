# GraphQL Multipart File Upload Starter

Spring Boot starter for handling GraphQL multipart file uploads with WebFlux and Spring for GraphQL.

## Specification

This library implements the GraphQL multipart request specification
defined by Jayden Seric:

https://github.com/jaydenseric/graphql-multipart-request-spec

This project is an independent implementation and is not affiliated
with or endorsed by the specification authors.

## Usage

Include the starter in your project.

Posts to the `/graphql` (by default - the handler is bound to the `spring.graphql.http.path` property) endpoint with 
content type of `multipart/form-data`and accepting `application/json` or `application/graphql-response+json` will be 
handled as multipart requests.  Files will be mapped as `FilePart` into variables in the GraphQL query as described in 
the specification.

### Notable differences from the specification
Batching of operations as described [here|https://github.com/jaydenseric/graphql-multipart-request-spec?tab=readme-ov-file#batching] 
is currently not supported.

## License
Licensed under the Apache License, Version 2.0.

## GPG Signing
Key fingerprint: 02FE 635B BD03 8EEF 0057  D378 D59E BBBD A551 2C1C
