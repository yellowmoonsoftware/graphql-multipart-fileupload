# Contributing to GraphQL Multipart File Upload Starter

Thank you for taking the time to contribute. Bug reports, feature proposals,
documentation improvements, tests, and code fixes are all welcome. Please keep
all project interactions respectful, constructive, and focused on improving the
project.

## Before Opening an Issue

Search the existing issues before opening a new one. Small documentation changes
and obvious fixes may be submitted directly as pull requests. Please open an
issue before starting substantial behavioral or API changes so the approach and
compatibility impact can be discussed first.

When reporting a bug, include:

- The library, Spring Boot, and Java versions in use.
- The expected and actual behavior.
- A minimal reproducer or representative multipart request, when possible.
- Relevant logs or stack traces with credentials and other sensitive data
  removed.

When proposing a feature, describe the use case, the desired behavior, and any
expected compatibility impact. Explain the problem independently of a preferred
implementation so alternative approaches can be considered.

## Development Setup

Development requires Maven and a JDK supported by the
[compatibility table](README.md#compatibility). Create a branch from `main` and
use these commands while working:

```shell
mvn test
```

Runs the test suite for rapid feedback.

```shell
mvn clean verify
```

Runs the complete build and generates the JaCoCo coverage report. Run this before
submitting a pull request.

```shell
mvn dependency:tree
```

Displays the dependency graph and is useful when reviewing dependency changes.
Continuous integration repeats the full verification build across the supported
Java versions.

## Code and Test Expectations

- Follow the existing Java style and package organization.
- Add focused JUnit 5 regression tests for behavioral changes. Avoid materially
  reducing test coverage.
- Follow the existing Javadoc conventions when changing public APIs.
- Preserve backward compatibility unless a change has explicitly been proposed
  and accepted as breaking.
- Update `README.md` when changing public behavior, configuration, compatibility,
  or usage.

## Commits and Pull Requests

Use [Conventional Commits](https://www.conventionalcommits.org/) for commit
messages and pull request titles. For example:

- `fix: handle an empty file map`
- `feat: support an additional upload mapping`
- `docs: clarify multipart request requirements`

Mark breaking changes with `!`, such as `feat!: change upload mapping behavior`,
and explain the migration impact in the pull request description.

Pull requests should:

- Target `main` and contain one focused change.
- Link related issues and explain the functional impact.
- Include the tests run and their results.
- Call out compatibility concerns or follow-up work.
- Pass `mvn clean verify` and all continuous integration jobs.

Do not manually update the project version or `CHANGELOG.md`; Release Please
manages release metadata from the Conventional Commit history. Contributions
are accepted under the repository's [license](LICENSE).
