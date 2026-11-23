# Tasks: Inbound and Outbound Adapters Delivery Layer Compliance

**Input**: Design documents from `/specs/003-adapters-delivery-compliance/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/rest-and-client-contracts.md`, `quickstart.md`

**Tests**: Unit tests for `OntologyRequestMapper`, `GeneOntologyController`, and updated `MeshOntologyControllerTest` are included to verify DTO binding, `MessageResponse` payloads, and mapper null-safety.

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) so each increment can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm baseline build before refactoring

- [X] T001 Verify baseline compilation and tests by running `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test` in repository root `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared DTOs and static mapper required by REST controllers

- [X] T002 [P] Create `MessageResponse(String message)` Java record in `src/main/java/com/biopatternsg/infrastructure/dtos/MessageResponse.java`
- [X] T003 [P] Create `BuildGeneOntologyTreeRequest(List<String> cellularComponent, List<String> molecularFunction, List<String> biologicalProcess)` Java record in `src/main/java/com/biopatternsg/infrastructure/dtos/BuildGeneOntologyTreeRequest.java`
- [X] T004 [P] Create `BuildMeshTreeRequest(String id, String name, String symbol, List<String> synonyms)` Java record in `src/main/java/com/biopatternsg/infrastructure/dtos/BuildMeshTreeRequest.java`
- [X] T005 Create stateless static mapper `OntologyRequestMapper` (`public final` utility class with private constructor and `public static requestToModel(...)` methods) in `src/main/java/com/biopatternsg/infrastructure/mapper/OntologyRequestMapper.java` (depends on T003, T004)

**Checkpoint**: Request/Response DTO records and `OntologyRequestMapper` are ready; user stories can proceed.

---

## Phase 3: User Story 1 - Typed Request & Response DTOs in REST Controllers (Priority: P1) 🎯 MVP

**Goal**: Replace direct domain-model request bindings and hardcoded JSON strings with typed DTO records (`BuildGeneOntologyTreeRequest`, `BuildMeshTreeRequest`, `MessageResponse`) and update OpenAPI `@Schema` annotations (fixing `"Buildind mesh tree"` -> `"Building mesh tree"`).

**Independent Test**: `MeshOntologyControllerTest`, `GeneOntologyControllerTest`, and `OntologyRequestMapperTest` confirm typed DTO binding, `MessageResponse` payloads (`202` and `404`), and corrected `"Building mesh tree"` message.

### Implementation for User Story 1

- [X] T006 [P] [US1] Update `GeneOntologyController#buildTreeGO` to accept `@Valid BuildGeneOntologyTreeRequest`, map via `OntologyRequestMapper.requestToModel(...)`, return `MessageResponse("Building Gene Ontology tree")`, and update `@Schema(implementation = MessageResponse.class)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/GeneOntologyController.java` (depends on T002, T005)
- [X] T007 [P] [US1] Update `MeshOntologyController#searchMeshId` (`404`) and `#buildTreeMesh` (`@Valid BuildMeshTreeRequest`, `MessageResponse("Building mesh tree")`, `@Schema(implementation = MessageResponse.class)`) in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/MeshOntologyController.java` (depends on T002, T005)
- [X] T008 [P] [US1] Update `shouldReturn404WhenNoMatchFound` to assert `MessageResponse` and add `buildTreeMesh` test asserting `202` + `MessageResponse("Building mesh tree")` in `src/test/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/MeshOntologyControllerTest.java` (depends on T007)
- [X] T009 [P] [US1] Add unit tests for `GeneOntologyController#buildTreeGO` (`202` + `MessageResponse("Building Gene Ontology tree")`) in `src/test/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/GeneOntologyControllerTest.java` (depends on T006)
- [X] T010 [P] [US1] Add unit tests for `OntologyRequestMapper` (`requestToModel` null handling and field mapping) in `src/test/java/com/biopatternsg/infrastructure/mapper/OntologyRequestMapperTest.java` (depends on T005)

**Checkpoint**: User Story 1 is complete and independently testable.

---

## Phase 4: User Story 2 - Single-Expression Lambdas in Controller Async & Optional Flows (Priority: P2)

**Goal**: Eliminate all multi-line lambdas in `GeneOntologyController` and `MeshOntologyController` by extracting private helper methods (`runBuildGeneOntologyTree`, `runSendBiologicalObjectToQueue`, `buildMeshNotFoundResponse`).

**Independent Test**: Neither controller contains multi-line lambdas inside `CompletableFuture.runAsync(...)` or `.orElseGet(...)`, and all controller tests pass.

### Implementation for User Story 2

- [X] T011 [P] [US2] Extract `private void runBuildGeneOntologyTree(GeneOntologyBuildTreeRequest request)` and use a single-expression lambda in `CompletableFuture.runAsync(...)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/GeneOntologyController.java` (depends on T006)
- [X] T012 [P] [US2] Extract `private Response buildMeshNotFoundResponse()` (for `.orElseGet(this::buildMeshNotFoundResponse)`) and `private void runSendBiologicalObjectToQueue(BiologicalObject biologicalObject)` (for `CompletableFuture.runAsync(...)`) in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/MeshOntologyController.java` (depends on T007)

**Checkpoint**: User Story 2 is complete with zero multi-line lambdas in controllers.

---

## Phase 5: User Story 3 - Accurate Internal HTTP Client Naming & Clean Consumer Adapter (Priority: P3)

**Goal**: Rename `BiologicalObjectsHttpClient#buildMeshOntologyTree` to `updateMeshId`, update `QueryBiologicalObjectsImpl`, and fix the `"Error updating mesh MeshId..."` log message typo.

**Independent Test**: `BiologicalObjectsHttpClient#updateMeshId` is invoked by `QueryBiologicalObjectsImpl`, `MeshQueueConsumerRabbitImpl` remains a clean standalone inbound adapter delegating to `BuildMeshOntologyTree`, and `./mvnw clean test` passes.

### Implementation for User Story 3

- [X] T013 [US3] Rename `buildMeshOntologyTree` to `updateMeshId` in `src/main/java/com/biopatternsg/infrastructure/clients/internal_clients/BiologicalObjectsHttpClient.java`
- [X] T014 [US3] Update `QueryBiologicalObjectsImpl` to call `biologicalObjectsHttpClient.updateMeshId(...)` and fix `"Error updating mesh MeshId..."` to `"Error updating meshId for biological object {}: {}"` in `src/main/java/com/biopatternsg/infrastructure/internal_services/impl/QueryBiologicalObjectsImpl.java` (depends on T013)

**Checkpoint**: User Story 3 is complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Full validation against `quickstart.md`

- [X] T015 Run full validation gates (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test` and static `grep` verification for `Buildind`, `buildMeshOntologyTree`, `mesh MeshId`) per `specs/003-adapters-delivery-compliance/quickstart.md`

---

## Dependencies & Execution Order

- **Phase 1 (`T001`)** -> **Phase 2 (`T002`–`T004` in parallel -> `T005`)** -> **Phase 3 (`US1`)**, **Phase 4 (`US2`)**, **Phase 5 (`US3`)** -> **Phase 6 (`T015`)**.
- Note: `T011` and `T012` can be applied cleanly alongside `T006` and `T007` respectively since they modify the same controller files.

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete `T001`–`T005`.
2. Complete `T006`–`T010` (`US1`) and validate with `./mvnw test`.

### Incremental Delivery
1. Deliver **US1** (typed Request/Response DTOs + `OntologyRequestMapper`).
2. Deliver **US2** (single-expression lambdas via private helpers).
3. Deliver **US3** (`BiologicalObjectsHttpClient#updateMeshId` rename + log fix).
4. Run **Phase 6** full verification.
