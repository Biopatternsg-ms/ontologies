# Tasks: Architecture and Documentation Governance Automated Tests

**Input**: Design documents from `/specs/005-arch-doc-governance/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/governance-rules.md`, `quickstart.md`

**Tests**: This feature implements automated architecture (`ArchitectureLayerTest`) and documentation governance (`DocumentationGovernanceTest`) tests.

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) so each increment can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm baseline build and existing test suite pass

- [X] T001 Verify baseline tests by running `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test` in repository root `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Confirm no blocking prerequisites

> All three user stories (`US1`, `US2`, `US3`) touch disjoint files and can proceed in parallel.

**Checkpoint**: Ready to implement User Stories 1, 2, and 3 in parallel.

---

## Phase 3: User Story 1 - Automated Architectural Boundary & Clean-Code Enforcement (Priority: P1) 🎯 MVP

**Goal**: Extend `ArchitectureLayerTest.java` with all 5 constitutional ArchUnit rules (`domainShouldNotDependOnFrameworksTest`, `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`, `useCasesShouldNotDependOnServiceImplementationsTest`, `staticMappersShouldBeStatelessFinalUtilityClassesTest`, `noFieldInjectionInApplicationAndAdaptersTest`).

**Independent Test**: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest` passes all ArchUnit rules with zero violations.

### Implementation for User Story 1

- [X] T002 [US1] Add `domainShouldNotDependOnFrameworksTest`, `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`, `useCasesShouldNotDependOnServiceImplementationsTest`, `staticMappersShouldBeStatelessFinalUtilityClassesTest`, and `noFieldInjectionInApplicationAndAdaptersTest` in `src/test/java/archtest/ArchitectureLayerTest.java`

**Checkpoint**: User Story 1 is complete and `ArchitectureLayerTest` passes.

---

## Phase 4: User Story 2 - Automated Use Case Documentation Governance Verification (Priority: P2)

**Goal**: Create `DocumentationGovernanceTest.java` verifying 1:1 Use Case <-> `.agents/use_cases/<UseCaseName>.md` synchronization, Mermaid `sequenceDiagram`, and index references in `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`.

**Independent Test**: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=DocumentationGovernanceTest` passes all assertions for all 5 Use Cases.

### Implementation for User Story 2

- [X] T003 [P] [US2] Create `DocumentationGovernanceTest` verifying every `*UseCase.java` has a matching `.agents/use_cases/<UseCaseName>.md` with `sequenceDiagram`, is indexed in `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`, and that no orphan `.md` files exist in `src/test/java/archtest/DocumentationGovernanceTest.java`

**Checkpoint**: User Story 2 is complete and `DocumentationGovernanceTest` passes.

---

## Phase 5: User Story 3 - Governance Gate Documentation Synchronization (Priority: P3)

**Goal**: Update verification gate documentation in `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` to include `DocumentationGovernanceTest`.

**Independent Test**: All three governance files reference `DocumentationGovernanceTest` in their verification gates sections, and `.specify/memory/constitution.md` includes a Sync Impact Report and `PATCH` version bump (`1.0.1`).

### Implementation for User Story 3

- [X] T004 [P] [US3] Add `./mvnw test -Dtest=DocumentationGovernanceTest` to Section 6.2 in `.agents/architecture-rules.md`
- [X] T005 [P] [US3] Add `./mvnw test -Dtest=DocumentationGovernanceTest` to Section 8 in `.agents/architecture.md`
- [X] T006 [P] [US3] Add `./mvnw test -Dtest=DocumentationGovernanceTest` to Development Workflow & Quality Gates and bump version to `1.0.1` with Sync Impact Report in `.specify/memory/constitution.md`

**Checkpoint**: User Story 3 is complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Run combined governance and full test suite

- [X] T007 Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest` and full `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test` per `specs/005-arch-doc-governance/quickstart.md`

---

## Dependencies & Execution Order

- **Phase 1 (`T001`)** -> **`T002` (`US1`), `T003` (`US2`), `T004`–`T006` (`US3`)** can all run in parallel -> **Phase 6 (`T007`)**.
