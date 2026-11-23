# Tasks: Domain Layer Purity and Framework Decoupling

**Input**: Design documents from `/specs/001-domain-layer-purity/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/internal-ports-and-dtos.md`, `quickstart.md`

**Tests**: Automated architecture verification (`ArchitectureLayerTest`) and unit tests for the refactored DTO/mapper behavior are included to verify constitutional compliance and null-safety.

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) so each increment can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish baseline build status and target directory layout

- [X] T001 Verify baseline compilation before refactoring by running `./mvnw clean compile` in repository root `pom.xml`
- [X] T002 [P] Create target package directory `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/` for relocated NCBI MeSH XML DTOs

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Confirm no shared cross-story blockers exist

> All three user stories (`US1`, `US2`, `US3`) touch disjoint sets of files (`BiologicalObjects*` & `MeshQueueConsumer*` in `US1`, `ESearchResult` / `ESummaryResult` / `PathsToRoot` / `PathToRoot` in `US2`, and `ESummaryResult.Item` cleanup in `US3`). No blocking foundational tasks are required before starting `US1` or `US2`.

**Checkpoint**: Ready to implement User Story 1 and User Story 2 in parallel.

---

## Phase 3: User Story 1 - Pure Domain Boundary for External Service & Messaging Ports (Priority: P1) 🎯 MVP

**Goal**: Remove `jakarta.ws.rs.core.Response` from `BiologicalObjectsRepository` and `BiologicalObjectsServiceImp`, and remove the misplaced `MeshQueueConsumer` (`io.vertx.core.json.JsonObject`) from `domain.port.out.consumers`.

**Independent Test**: Confirm `BiologicalObjectsRepository`, `BiologicalObjectsServiceImp`, and `domain.port.out.consumers` contain zero `jakarta.ws.rs.*` or `io.vertx.*` imports, `BiologicalObjectsAdapter` closes `Response` with `try-with-resources`, `MeshQueueConsumerRabbitImpl` compiles as a standalone inbound adapter invoking `BuildMeshOntologyTree`, and `./mvnw clean compile` passes.

### Implementation for User Story 1

- [X] T003 [P] [US1] Change `updateMeshId(String biologicalObjectId, String meshId)` return type from `Response` to `void` and remove `jakarta.ws.rs.core.Response` import in `src/main/java/com/biopatternsg/domain/port/out/external_repositories/BiologicalObjectsRepository.java`
- [X] T004 [P] [US1] Remove `implements MeshQueueConsumer` and its import from `src/main/java/com/biopatternsg/infrastructure/adapters/in/consumers/MeshQueueConsumerRabbitImpl.java`
- [X] T005 [US1] Update `updateMeshId` to delegate to `biologicalObjectsRepository.updateMeshId(biologicalObjectId, meshId)` without `Response` handling or `jakarta.ws.rs.core.Response` import in `src/main/java/com/biopatternsg/application/services/impl/BiologicalObjectsServiceImp.java` (depends on T003)
- [X] T006 [US1] Update `BiologicalObjectsAdapter.updateMeshId` to return `void`, wrap `queryBiologicalObjects.updateMeshId(...)` in `try (Response response = ...)`, and log status when `response != null` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/external_repositories/BiologicalObjectsAdapter.java` (depends on T003)
- [X] T007 [US1] Delete `src/main/java/com/biopatternsg/domain/port/out/consumers/MeshQueueConsumer.java` (depends on T004)

**Checkpoint**: User Story 1 is complete and independently compilable/testable.

---

## Phase 4: User Story 2 - Decoupled External Ontology Serialization Models (Priority: P2)

**Goal**: Relocate `ESearchResult` and `ESummaryResult` from `domain.model.mesh` to `infrastructure.external_services.dto.mesh`, and decouple `PathsToRoot` and `PathToRoot` from Jackson by introducing `PathsToRootResponse` and `PathToRootResponse` records mapped via `GoTermMapper`.

**Independent Test**: Confirm `domain.model.*` has zero `jakarta.xml.bind.*`, `com.fasterxml.jackson.*`, `io.quarkus.*`, or `org.w3c.dom.*` imports, `GeneOntologyHttpClient` / `QueryGO` / `GOAdapter` use `PathsToRootResponse` mapped to `PathsToRoot` via `GoTermMapper`, and `./mvnw clean compile` passes.

### Implementation for User Story 2

- [X] T008 [P] [US2] Move `ESearchResult` from `src/main/java/com/biopatternsg/domain/model/mesh/ESearchResult.java` to `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESearchResult.java` and update its package declaration
- [X] T009 [P] [US2] Move `ESummaryResult` from `src/main/java/com/biopatternsg/domain/model/mesh/ESummaryResult.java` to `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESummaryResult.java` and update its package declaration
- [X] T010 [P] [US2] Remove `@JsonIgnoreProperties(ignoreUnknown = true)` and `com.fasterxml.jackson.annotation.JsonIgnoreProperties` import from `src/main/java/com/biopatternsg/domain/model/PathsToRoot.java`
- [X] T011 [P] [US2] Remove `@JsonIgnoreProperties(ignoreUnknown = true)` and `com.fasterxml.jackson.annotation.JsonIgnoreProperties` import from `src/main/java/com/biopatternsg/domain/model/PathToRoot.java`
- [X] T012 [P] [US2] Create `@JsonIgnoreProperties(ignoreUnknown = true)` Java record `PathToRootResponse(String child, String parent, String relationship)` in `src/main/java/com/biopatternsg/infrastructure/external_services/dto/go/PathToRootResponse.java`
- [X] T013 [US2] Create `@JsonIgnoreProperties(ignoreUnknown = true)` Java record `PathsToRootResponse(int numberOfHits, List<List<PathToRootResponse>> results)` in `src/main/java/com/biopatternsg/infrastructure/external_services/dto/go/PathsToRootResponse.java` (depends on T012)
- [X] T014 [US2] Update `ESearchResult` and `ESummaryResult` imports to `com.biopatternsg.infrastructure.external_services.dto.mesh.*` in `src/main/java/com/biopatternsg/infrastructure/clients/external_clients/MeshOntologyHttpClient.java` (depends on T008, T009)
- [X] T015 [US2] Update `ESearchResult` and `ESummaryResult` imports to `com.biopatternsg.infrastructure.external_services.dto.mesh.*` in `src/main/java/com/biopatternsg/infrastructure/external_services/impl/QueryMeshImpl.java` (depends on T008, T009)
- [X] T016 [US2] Update `getPathsToRoot` return type to `PathsToRootResponse` in `src/main/java/com/biopatternsg/infrastructure/clients/external_clients/GeneOntologyHttpClient.java` (depends on T013)
- [X] T017 [US2] Update `getPathsToRoot` return type to `PathsToRootResponse` in `src/main/java/com/biopatternsg/infrastructure/external_services/QueryGO.java` and `src/main/java/com/biopatternsg/infrastructure/external_services/impl/QueryGoImpl.java` (depends on T016)
- [X] T018 [US2] Add null-safe `toPathsToRoot(PathsToRootResponse response)` mapping method in `src/main/java/com/biopatternsg/infrastructure/mapper/GoTermMapper.java` (depends on T010, T011, T013)
- [X] T019 [US2] Update `GOAdapter.getPathsToRoot(String goId)` to map `queryGO.getPathsToRoot(goId)` via `goTermMapper.toPathsToRoot(...)` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/external_repositories/GOAdapter.java` (depends on T017, T018)

**Checkpoint**: User Story 2 is complete and all domain models are free of serialization, reflection, and XML DOM dependencies.

---

## Phase 5: User Story 3 - Clean & Null-Safe External Summary Extraction (Priority: P3)

**Goal**: Clean up `ESummaryResult.Item` by removing the unused `getSubItemValues()` method, renaming `getValuesBySubItemNameOtra` to `getValuesBySubItemNameOrText`, and adding null guards for `content` and DOM child/text nodes, with unit test coverage.

**Independent Test**: Unit tests for `ESummaryResult.Item` pass for matching sub-items, text-only elements, null `content`, and null/blank text nodes without throwing `NullPointerException`.

### Implementation for User Story 3

- [X] T020 [US3] Remove dead `getSubItemValues()`, rename `getValuesBySubItemNameOtra` to `getValuesBySubItemNameOrText`, and add null guards for `content` and `getTextContent()` in `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESummaryResult.java` (depends on T009)
- [X] T021 [US3] Update `it.getValuesBySubItemNameOtra("string")` call to `it.getValuesBySubItemNameOrText("string")` in `src/main/java/com/biopatternsg/infrastructure/external_services/impl/QueryMeshImpl.java` (depends on T020)
- [X] T022 [P] [US3] Add unit tests for `ESummaryResult.Item` (`getValuesBySubItemName`, `getValuesBySubItemNameOrText`, null `content`, missing child nodes) in `src/test/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESummaryResultTest.java` (depends on T020)
- [X] T023 [P] [US3] Add unit tests for `GoTermMapper.toPathsToRoot` (null input, null results, valid nested paths) in `src/test/java/com/biopatternsg/infrastructure/mapper/GoTermMapperTest.java` (depends on T018)

**Checkpoint**: User Story 3 is complete with clean English naming and verified null-safety.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Enforce architectural purity via ArchUnit and run full validation gates from `quickstart.md`

- [X] T024 Add ArchUnit rules prohibiting `domain..` from depending on `jakarta.ws.rs..`, `io.vertx..`, `io.quarkus..`, `jakarta.xml.bind..`, `com.fasterxml.jackson..`, `org.bson..`, or `org.w3c.dom..` and prohibiting `application..` from depending on `jakarta.ws.rs..` or `io.vertx..` in `src/test/java/archtest/ArchitectureLayerTest.java`
- [X] T025 Run full validation gates (`./mvnw clean compile`, `./mvnw test -Dtest=ArchitectureLayerTest`, `./mvnw test`, and static import `grep` checks from `specs/001-domain-layer-purity/quickstart.md`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: No blocking tasks.
- **User Story 1 (Phase 3)** and **User Story 2 (Phase 4)**: Can proceed in parallel after Phase 1.
- **User Story 3 (Phase 5)**: Depends on `T009` (relocation of `ESummaryResult`) and `T018` (`GoTermMapper.toPathsToRoot` for `T023`).
- **Polish (Phase 6)**: Depends on all user stories (`Phase 3`, `Phase 4`, `Phase 5`) being complete.

### Parallel Opportunities

- Within **US1**: `T003` and `T004` can run in parallel; `T005` and `T006` can run in parallel once `T003` is done.
- Within **US2**: `T008`, `T009`, `T010`, `T011`, and `T012` can all run in parallel; `T014` and `T015` can run in parallel once `T008`/`T009` are done.
- Within **US3**: `T022` and `T023` can run in parallel with `T021` once `T020` and `T018` are complete.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (`T001`–`T002`).
2. Complete Phase 3 (`T003`–`T007`) to eliminate HTTP `Response` and Vert.x `JsonObject` from `domain` and `application`.
3. Validate with `./mvnw clean compile` and `./mvnw test -Dtest=ArchitectureLayerTest`.

### Incremental Delivery

1. Deliver **US1** (port & messaging decoupling).
2. Deliver **US2** (NCBI XML DTO relocation & QuickGO Jackson decoupling).
3. Deliver **US3** (`ESummaryResult.Item` cleanup & unit tests).
4. Complete **Polish** (`ArchitectureLayerTest` framework-isolation rules + full `./mvnw test`).
