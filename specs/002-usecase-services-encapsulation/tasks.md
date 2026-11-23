# Tasks: Use Cases Lightweight Orchestration and Application Services Encapsulation

**Input**: Design documents from `/specs/002-usecase-services-encapsulation/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/internal-contracts.md`, `quickstart.md`

**Tests**: Unit tests for the refactored use cases and updated repository stubs are included to verify non-mutation, pre-check ordering, and behavioral equivalence.

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) so each increment can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm clean baseline build before refactoring

- [X] T001 Verify baseline compilation and tests by running `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test` in repository root `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared non-mutating name-resolution helper used by multiple use cases

- [X] T002 Create stateless `BiologicalObjectNameResolver` with `public static Optional<String> resolveEffectiveName(BiologicalObject biologicalObject)` (without mutating `biologicalObject`) in `src/main/java/com/biopatternsg/application/services/BiologicalObjectNameResolver.java`

**Checkpoint**: Shared name-resolution helper is ready; user stories can proceed in parallel.

---

## Phase 3: User Story 1 - Lightweight Use Case Orchestration & Non-Mutating Name Resolution (Priority: P1) 🎯 MVP

**Goal**: Eliminate `BiologicalObject` input mutation, share `BiologicalObjectNameResolver.resolveEffectiveName`, check local existence before NCBI calls in `BuildMeshOntologyTreeUseCase`, replace `Stack` with `ArrayDeque`, fix `saveMechTerm` -> `saveMeshTerm`, decompose private helpers, and make package-private helpers `private`.

**Independent Test**: Unit tests for `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase` confirm `BiologicalObject` is never mutated, local existence is checked before external NCBI calls, and all tests pass.

### Implementation for User Story 1

- [X] T003 [P] [US1] Refactor `SendBiologicalObjectToQueueUseCase` to use `BiologicalObjectNameResolver.resolveEffectiveName(biologicalObject)` without mutating `biologicalObject` and make `isSavedName` `private` in `src/main/java/com/biopatternsg/application/usecase/SendBiologicalObjectToQueueUseCase.java`
- [X] T004 [P] [US1] Refactor `BuildMeshOntologyTreeUseCase` to use `BiologicalObjectNameResolver.resolveEffectiveName(biologicalObject)` without mutating `biologicalObject`, check `isSavedName(effectiveName)` before calling `meshOntologyService.getMeshTermIds(...)`, replace `Stack<String>` with `Deque<String> = new ArrayDeque<>()`, rename `saveMechTerm` to `saveMeshTerm`, extract `saveAncestorTree` / `buildAncestorMeshInfo` private helpers, and make `findFirstMatchTerm`, `isSavedName`, and `isSavedTerm` `private` in `src/main/java/com/biopatternsg/application/usecase/BuildMeshOntologyTreeUseCase.java`
- [X] T005 [P] [US1] Add unit tests verifying non-mutation of `BiologicalObject`, symbol fallback, and early exit when already saved in `src/test/java/com/biopatternsg/application/usecase/SendBiologicalObjectToQueueUseCaseTest.java` (depends on T003)
- [X] T006 [P] [US1] Add unit tests verifying non-mutation of `BiologicalObject` and early exit before NCBI calls when `isSavedName` is true in `src/test/java/com/biopatternsg/application/usecase/BuildMeshOntologyTreeUseCaseTest.java` (depends on T004)

**Checkpoint**: User Story 1 is complete and independently testable.

---

## Phase 4: User Story 2 - Deduplicated MeSH Category Traversal & Clean GO Enrichment (Priority: P2)

**Goal**: Consolidate duplicated BFS ancestor traversal in `CheckMeshTermTypeUseCase` and clean up `BuildGeneOntologyTreeUseCase` (`Set<String>` for `termIdsInDB` and `enrichAndSaveGoTerm` helper).

**Independent Test**: `CheckMeshTermTypeUseCaseTest` passes all 16 existing tests with zero duplicated BFS loops, and `BuildGeneOntologyTreeUseCase` compiles and passes architecture checks.

### Implementation for User Story 2

- [X] T007 [P] [US2] Extract shared private BFS helper `traverseAncestors(String startId, MeshInfo startTerm, Function<MeshInfo, Boolean> shouldContinue)` and `enqueueParents(MeshInfo term, Queue<String> queue, Set<String> visited)` in `src/main/java/com/biopatternsg/application/usecase/CheckMeshTermTypeUseCase.java`
- [X] T008 [P] [US2] Replace `List<String> termIdsInDB` with `Set<String>` in `getNewGOTermIds(...)` and replace the multi-line `.forEach` lambda in `execute(...)` with a `for` loop calling private `enrichAndSaveGoTerm(GoTerm goTerm)` in `src/main/java/com/biopatternsg/application/usecase/BuildGeneOntologyTreeUseCase.java`

**Checkpoint**: User Story 2 is complete and `CheckMeshTermTypeUseCaseTest` passes.

---

## Phase 5: User Story 3 - Consistent Service Naming & Strict Repository Contracts (Priority: P3)

**Goal**: Rename `BiologicalObjectsServiceImp` to `BiologicalObjectsServiceImpl` and remove silent no-op `default {}` implementations from `MeshOntologyRepository.updateCategories` and `MeshOntologyRepository.updateCategory`.

**Independent Test**: `BiologicalObjectsServiceImpl` is recognized by CDI and `ArchitectureLayerTest`, and all `MeshOntologyRepository` implementations/test stubs explicitly implement `updateCategories` and `updateCategory`.

### Implementation for User Story 3

- [X] T009 [P] [US3] Rename `BiologicalObjectsServiceImp` to `BiologicalObjectsServiceImpl` by creating `src/main/java/com/biopatternsg/application/services/impl/BiologicalObjectsServiceImpl.java` and deleting `src/main/java/com/biopatternsg/application/services/impl/BiologicalObjectsServiceImp.java`
- [X] T010 [P] [US3] Remove `default {}` bodies from `updateCategories(String meshId, Map<String, Boolean> categories)` and `updateCategory(String meshId, String category, boolean isType)` in `src/main/java/com/biopatternsg/domain/port/out/repositories/MeshOntologyRepository.java`
- [X] T011 [US3] Add explicit `updateCategories` and `updateCategory` method implementations to `StubMeshOntologyRepository` in `src/test/java/com/biopatternsg/application/usecase/SearchMeshIdBySynonymsUseCaseTest.java` (depends on T010)

**Checkpoint**: User Story 3 is complete with strict repository contracts and consistent `*Impl` naming.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Full verification against `quickstart.md`

- [X] T012 Run full validation gates (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test` and static `grep` verification for `java.util.Stack`, `saveMechTerm`, and `BiologicalObjectsServiceImp`) per `specs/002-usecase-services-encapsulation/quickstart.md`

---

## Dependencies & Execution Order

- **Phase 1 (Setup)** -> **Phase 2 (`T002`)** -> **Phase 3 (`US1`), Phase 4 (`US2`), Phase 5 (`US3`)** can run in parallel -> **Phase 6 (`T012`)**.
- Within **US1**: `T003` and `T004` run in parallel; `T005` depends on `T003`; `T006` depends on `T004`.
- Within **US2**: `T007` and `T008` run in parallel.
- Within **US3**: `T009` and `T010` run in parallel; `T011` depends on `T010`.

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete `T001`–`T002`.
2. Complete `T003`–`T006` (`US1`) and validate with `./mvnw test`.

### Incremental Delivery
1. Deliver **US1** (non-mutating name resolution + `BuildMeshOntologyTreeUseCase` cleanup).
2. Deliver **US2** (BFS deduplication + `BuildGeneOntologyTreeUseCase` cleanup).
3. Deliver **US3** (`BiologicalObjectsServiceImpl` rename + strict `MeshOntologyRepository` contract).
4. Run **Phase 6** full verification.
