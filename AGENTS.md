# Repository Guidelines

## Project Structure & Module Organization
- Core sources live in `src/main/java/com/yellowmoon/graphql/multipart`, grouped by role: `config` for Spring auto-configuration, `multipartmapper` for request parsing, and support utilities under `util`.
- Shared resources sit in `src/main/resources/META-INF`, including the auto-configuration import list needed for Spring Boot starters.
- Tests mirror the production packages in `src/test/java`, using dedicated fixtures (`MockFilePart`, `GqlTestData`, etc.) alongside unit specs.
- Maven build output appears in `target/`; never commit its contents.

## Build, Test & Development Commands
- `mvn clean install` — compile with Java 23, run the full test suite, and publish the starter JAR to the local Maven cache.
- `mvn test` — execute unit and slice tests without creating reports, ideal for rapid iteration.
- `mvn verify` — run tests and generate the JaCoCo coverage report (`target/site/jacoco`), required before tagging releases.
- `mvn dependency:tree` — inspect starter dependencies when reviewing transitive upgrades.

## Coding Style & Naming Conventions
- Follow standard Java style with 4-space indentation and braces on new lines; rely on IDE formatters or your preferred formatter before committing.
- Keep packages aligned with existing domains (`config`, `multipartmapper`, `util`); new public APIs should live under `com.yellowmoon.graphql.multipart`.
- Prefer expressive method names and immutable DTOs; use Lombok sparingly and document any generated methods.
- Spring beans should end in `Config`, `Extractor`, or `Handler` to match current patterns.
- Documentation style for Java:
  - Use Javadoc style (`/** ... */`) for public APIs.
  - Start class-level docs with a `<h2>ClassName</h2>` header.
  - Use `@param` for method parameters and `@return` for return values.
  - Document `@link`s to external resources, `@see`s to local methods.
  - Document all generic type parameters.
  - Do not leave blank lines - use paragraph breaks instead.

## Testing Guidelines
- Write JUnit 5 tests (`@Test` from `org.junit.jupiter.api`) and Mockito extensions where mocks are necessary.
- Name tests `<ClassName>Test` to retain Maven Surefire discovery; helper fixtures may end in `Mock` or `TestConfig`.
- Guard new behaviours with focused unit tests; aim to keep coverage at the current Jacoco baseline (~90%) before requesting review.
- Run `mvn verify` locally when touching request parsing or WebFlux integration to ensure regression protection.

## Commit & Pull Request Guidelines
- Use concise, imperative commit messages capped near 60 characters (e.g., `Fix auto configuration`, `Add UploadScalar test`), mirroring the existing history.
- Squash or rebase before opening a PR so each commit represents a coherent change.
- PRs should describe the functional impact, reference linked issues, and include test evidence or follow-up tasks; attach JSON/XML examples when altering request mapping.
- Highlight backwards-incompatible changes and document new configuration properties in the PR description and README.
