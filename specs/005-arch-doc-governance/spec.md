# Feature Specification: Architecture and Documentation Governance Automated Tests

**Feature Branch**: `005-arch-doc-governance`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/ontologies/05-architecture-and-documentation-governance-tests"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Automated Architectural Boundary & Clean-Code Enforcement (Priority: P1)

As a service maintainer, I want automated architecture tests to enforce domain/application framework isolation, use-case-to-service-implementation decoupling, stateless static mapper constraints, and constructor-injection hygiene so that any future architectural drift fails the build immediately.

**Why this priority**: Without automated checks for framework leakage, concrete service implementation imports in use cases, mapper CDI annotations, and `@Inject` field injection, regressions can silently re-enter the codebase.

**Independent Test**: Can be verified by running the architecture test suite (`ArchitectureLayerTest`) and confirming all new and existing rules pass against the compliant codebase.

**Acceptance Scenarios**:

1. **Given** the domain and application packages, **When** `ArchitectureLayerTest` executes, **Then** it verifies that `domain..` has zero dependencies on web, CDI, XML, JSON, Quarkus, Vert.x, MongoDB/BSON, or MicroProfile packages, and `application..` has zero dependencies on delivery or persistence frameworks.
2. **Given** the use cases, static mappers, and adapters packages, **When** `ArchitectureLayerTest` executes, **Then** it verifies that `application.usecase..` does not depend on `application.services.impl..`, `infrastructure.mapper..` classes are `final` with `private` constructors and no CDI scope annotations, and `application..` / `infrastructure.adapters..` contain zero `@Inject` field injections.

---

### User Story 2 - Automated Use Case Documentation Governance Verification (Priority: P2)

As a service maintainer, I want an automated governance test (`DocumentationGovernanceTest`) to verify that every Use Case has a corresponding documentation file with a Mermaid `sequenceDiagram`, is indexed in the use-cases README and architecture guide, and that no orphan use-case documentation files exist so that documentation stays synchronized with code.

**Why this priority**: Manual verification of use-case documentation and index entries is error-prone; automating it guarantees every new or refactored Use Case remains documented and discoverable.

**Independent Test**: Can be verified by running `DocumentationGovernanceTest` and confirming all 5 existing Use Cases pass every documentation, Mermaid diagram, index, and orphan-check assertion.

**Acceptance Scenarios**:

1. **Given** all `*UseCase.java` classes in `application/usecase/`, **When** `DocumentationGovernanceTest` runs, **Then** it confirms each Use Case has a matching `.agents/use_cases/<UseCaseName>.md` file containing a Mermaid `sequenceDiagram` block, and is referenced in both `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`.
2. **Given** all `.md` files in `.agents/use_cases/` (excluding `README.md`), **When** `DocumentationGovernanceTest` runs, **Then** it confirms no orphan documentation file exists without a corresponding `*UseCase.java` class.

---

### User Story 3 - Governance Gate Documentation Synchronization (Priority: P3)

As a service maintainer, I want the mandatory verification gate documentation (`.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md`) updated to include `DocumentationGovernanceTest` so that contributors and AI agents run both architecture and documentation governance gates before completing changes.

**Why this priority**: Keeps project governance documents aligned with the automated test suite.

**Independent Test**: Can be verified by inspecting `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` for the `DocumentationGovernanceTest` verification command and confirming `./mvnw test` runs cleanly.

**Acceptance Scenarios**:

1. **Given** the governance documentation files, **When** their verification gates sections are inspected, **Then** `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` all include `./mvnw test -Dtest=DocumentationGovernanceTest` (or `ArchitectureLayerTest,DocumentationGovernanceTest`).

---

### Edge Cases

- What happens if a new `*UseCase.java` is added without a `.agents/use_cases/<UseCaseName>.md` file or without a `sequenceDiagram`? `DocumentationGovernanceTest` fails with a clear message identifying the missing file or missing diagram.
- What happens if a Use Case is deleted or renamed without removing its old `.agents/use_cases/<OldName>.md`? The orphan-check in `DocumentationGovernanceTest` fails, reporting the orphan `.md` file.
- What happens if an inner/anonymous/synthetic class exists in `infrastructure.mapper..`? `staticMappersShouldBeStatelessFinalUtilityClassesTest` checks top-level mapper classes (or non-synthetic classes) cleanly without false positives.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `ArchitectureLayerTest` MUST enforce `domainShouldNotDependOnFrameworksTest` prohibiting `com.biopatternsg.domain..` from depending on `jakarta.ws.rs..`, `jakarta.enterprise..`, `jakarta.inject..`, `jakarta.xml.bind..`, `com.fasterxml.jackson..`, `io.quarkus..`, `io.vertx..`, `org.bson..`, or `org.eclipse.microprofile..`.
- **FR-002**: `ArchitectureLayerTest` MUST enforce `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest` prohibiting `com.biopatternsg.application..` from depending on `jakarta.ws.rs..`, `io.vertx..`, `io.quarkus.mongodb..`, or `org.bson..`.
- **FR-003**: `ArchitectureLayerTest` MUST enforce `useCasesShouldNotDependOnServiceImplementationsTest` prohibiting `com.biopatternsg.application.usecase..` from depending on `com.biopatternsg.application.services.impl..`.
- **FR-004**: `ArchitectureLayerTest` MUST enforce `staticMappersShouldBeStatelessFinalUtilityClassesTest` verifying classes in `..infrastructure.mapper..` are `final`, declare only `private` constructors, and have no `@ApplicationScoped`, `@Singleton`, or `@RequestScoped` annotations.
- **FR-005**: `ArchitectureLayerTest` MUST enforce `noFieldInjectionInApplicationAndAdaptersTest` verifying no fields in `com.biopatternsg.application..` or `com.biopatternsg.infrastructure.adapters..` are annotated with `@Inject`.
- **FR-006**: `DocumentationGovernanceTest` MUST be created under `src/test/java/archtest/` to verify that every `*UseCase.java` has a matching `.agents/use_cases/<UseCaseName>.md` with a Mermaid `sequenceDiagram`, is referenced in `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`, and that no orphan `.md` files exist in `.agents/use_cases/`.
- **FR-007**: `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` MUST include `DocumentationGovernanceTest` in their verification gates.

### Key Entities

- **`ArchitectureLayerTest`**: ArchUnit test suite enforcing hexagonal boundaries, framework purity, static mappers, and constructor injection.
- **`DocumentationGovernanceTest`**: Automated JUnit 5 test verifying bidirectional synchronization between `*UseCase.java` classes and `.agents/use_cases/` documentation + index files.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the 5 new architectural rules in `ArchitectureLayerTest` pass with zero violations.
- **SC-002**: 100% of existing Use Cases pass all 5 documentation governance checks in `DocumentationGovernanceTest`.
- **SC-003**: 100% of verification gate sections in `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` reference `DocumentationGovernanceTest`.
- **SC-004**: `./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest` and full `./mvnw test` pass with zero errors.

## Assumptions

- `ArchitectureLayerTest` already has `domainLayerFrameworkPurityTest` and `applicationLayerFrameworkPurityTest` from `001-domain-layer-purity`; we will align/extend them with the exact rules and names specified in `REQ-ONT-ARCH-05` (`domainShouldNotDependOnFrameworksTest`, `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`, `useCasesShouldNotDependOnServiceImplementationsTest`, `staticMappersShouldBeStatelessFinalUtilityClassesTest`, `noFieldInjectionInApplicationAndAdaptersTest`).
- Updating `.specify/memory/constitution.md` will include a Sync Impact Report comment and a `PATCH` version bump (`1.0.0` -> `1.0.1`) per the constitution's amendment procedure.
