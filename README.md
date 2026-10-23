# GraphQL Multipart File Upload Starter

Spring Boot starter for handling GraphQL multipart file uploads with WebFlux and Spring for GraphQL.

## Compatibility

| Library version | Spring Boot version | Minimum Java version |
| --- | --- | --- |
| 1.0.4 | 3.5.16 | 17 |
| 2.0.0 | 4.1.0 | 17 |

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

### Batched operations

The `operations` field can contain an array of GraphQL requests. File map paths then
start with the zero-based operation index, followed by `variables` and the variable
path. One file can be shared by multiple variables or operations:

```sh
curl http://localhost:8080/graphql \
  -H 'Accept: application/json' \
  -F 'operations=[{"query":"mutation($file: Upload!) { singleUpload(file: $file) { id } }","variables":{"file":null}},{"query":"mutation($file: Upload!) { singleUpload(file: $file) { id } }","variables":{"file":null}}]' \
  -F 'map={"upload":["0.variables.file","1.variables.file"]}' \
  -F 'upload=@example.txt'
```

Up to eight operations execute concurrently through Spring's `WebGraphQlHandler`,
including its interceptors. The response is a JSON array in the same order as the
input operations, even when operations finish in a different order. An array with
one operation still receives an array response. A single operations object retains
the existing object response and media type negotiation.

Batch responses use `application/json`; the `Accept` header must allow that type
(or a compatible wildcard). If it does not, the handler returns HTTP 406 before
executing operations. GraphQL errors are returned at the corresponding array
position and do not prevent other operations from executing. Batches are not
transactions, and their mutations must not rely on execution order. Unexpected
execution failures retain normal HTTP error handling.

Batches must be non-empty arrays of request objects and include a `map` part
(use `{}` when no files are mapped). Invalid request fields, missing mapped files,
invalid or conflicting paths, and destinations without an existing `null`
placeholder return HTTP 400 before execution. Existing single-operation mapping
retains its lenient treatment of missing file maps and invalid paths.

Response headers are merged in operation order. For repeated header names, the
first operation providing that header wins; `Set-Cookie` values are appended from
every operation. Content type and HTTP framing are set for the combined response.

Custom decoders can implement `GraphQlMultipartDecoder.decodePayload` to return a
`GraphQlMultipartPayload` containing ordered requests and a batch flag. Its default
implementation adapts existing `decode` implementations as single requests. The
built-in decoder's `decode` method remains single-operation-only and rejects arrays;
use `decodePayload` for either payload shape.

### Supported upload scope

Files are mapped only into `variables`, including nested objects and lists.
Multipart data is fully received before execution starts. Streaming execution and
resolver-driven upload cancellation are not currently supported.

## Dependency updates

[Dependabot](.github/dependabot.yml) checks Maven dependencies and plugins in `pom.xml`
and GitHub Actions versions every Monday. It opens individual update PRs, with a limit
of five open version-update PRs per ecosystem, using `chore(deps)` commit titles.
The existing CI workflow builds and tests each PR on Java 17, 21, and 25.
Version updates start once the configuration is merged into the default branch.

## License
Licensed under the Apache License, Version 2.0.

## GPG Signing
Key fingerprint: 02FE 635B BD03 8EEF 0057  D378 D59E BBBD A551 2C1C
