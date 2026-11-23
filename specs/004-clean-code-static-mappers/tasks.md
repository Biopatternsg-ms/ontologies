# Tasks: Clean Code Lambdas, Dependency Injection, Null Safety and Static Mappers

**Input**: Design documents from `/specs/004-clean-code-static-mappers/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/mapper-and-di-contracts.md`, `quickstart.md`

**Tests**: Unit tests for `GoTermMapper`, `MeshTermMapper`, and `GeneOntologyBuildTreeRequest` are included to verify static mapping, null-safety, and `@ToString`.

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

**Purpose**: Confirm no cross-story blockers exist

> All three user stories (`US1`, `US2`, `US3`) touch disjoint files and can proceed in parallel after Phase 1.

**Checkpoint**: Ready to implement User Stories 1, 2, and 3.

---

## Phase 3: User Story 1 - Stateless Static Mappers & Pure Stream Transformations (Priority: P1) 🎯 MVP

**Goal**: Refactor `GoTermMapper` and create `MeshTermMapper` as `public final` static utility classes with zero CDI annotations, `.toList()`, `List.of()` for null collections, and single-expression lambdas; update `GOAdapter`, `GeneOntologyTermRepositoryAdapter`, and `MeshOntologyRepositoryAdapter`.

**Independent Test**: `GoTermMapperTest` and `MeshTermMapperTest` pass, zero `Collectors.toList()` or CDI annotations remain on mappers, and `MeshOntologyRepositoryAdapter` uses `toCaseInsensitiveFilters`.

### Implementation for User Story 1

- [X] T002 [P] [US1] Refactor `GoTermMapper` to `public final class GoTermMapper` with private constructor, zero CDI annotations, `public static` null-safe methods (`quickGoToModelList`, `collectionToModel`, `modelToCollection`, `pathsToRootToModel`), `.toList()`, `List.of()` for null collections, and single-expression helper calls in `src/main/java/com/biopatternsg/infrastructure/mapper/GoTermMapper.java`
- [X] T003 [P] [US1] Create `public final class MeshTermMapper` with private constructor and `public static` null-safe methods (`collectionToModel`, `modelToCollection`) in `src/main/java/com/biopatternsg/infrastructure/mapper/MeshTermMapper.java`
- [X] T004 [US1] Remove injected `GoTermMapper` field and invoke `GoTermMapper.pathsToRootToModel` and `GoTermMapper.quickGoToModelList` statically in `src/main/java/com/biopatternsg/infrastructure/adapters/out/external_repositories/GOAdapter.java` (depends on T002)
- [X] T005 [US1] Remove injected `GoTermMapper` field and invoke `GoTermMapper.collectionToModel` and `GoTermMapper.modelToCollection` statically in `src/main/java/com/biopatternsg/infrastructure/adapters/out/GeneOntologyTermRepositoryAdapter.java` (depends on T002)
- [X] T006 [US1] Replace inline `toModel`/`toEntity` with `MeshTermMapper.collectionToModel`/`modelToCollection` and extract `private Stream<Bson> toCaseInsensitiveFilters(String synonym)` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/MeshOntologyRepositoryAdapter.java` (depends on T003)
- [X] T007 [P] [US1] Update `GoTermMapperTest` to test static `GoTermMapper.pathsToRootToModel`, `quickGoToModelList`, `collectionToModel`, and `modelToCollection` with null-safety in `src/test/java/com/biopatternsg/infrastructure/mapper/GoTermMapperTest.java` (depends on T002)
- [X] T008 [P] [US1] Add unit tests for `MeshTermMapper.collectionToModel` and `modelToCollection` (null input, null lists, populated fields) in `src/test/java/com/biopatternsg/infrastructure/mapper/MeshTermMapperTest.java` (depends on T003)

**Checkpoint**: User Story 1 is complete and independently testable.

---

## Phase 4: User Story 2 - Constructor Injection Hygiene & Thread Interrupt Restoration (Priority: P2)

**Goal**: Standardize constructor injection in `MeshQueueSenderRabbitAdapter` and `QueryGoImpl`, and restore `Thread.currentThread().interrupt()` in `QueryMeshImpl`.

**Independent Test**: Zero `@Inject` field injections remain in `infrastructure.adapters.*` or `QueryGoImpl`, and `QueryMeshImpl` restores the interrupt flag on `InterruptedException`.

### Implementation for User Story 2

- [X] T009 [P] [US2] Refactor `MeshQueueSenderRabbitAdapter` to use `private final Emitter<BiologicalObject> emitter;` with explicit constructor `public MeshQueueSenderRabbitAdapter(@Channel("ncbi-out") Emitter<BiologicalObject> emitter)` and remove unused `@RequiredArgsConstructor` and field `@Inject` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/producers/MeshQueueSenderRabbitAdapter.java`
- [X] T010 [P] [US2] Remove redundant field-level `@RestClient` and `@Inject` annotations from `goHttpClient` in `src/main/java/com/biopatternsg/infrastructure/external_services/impl/QueryGoImpl.java`
- [X] T011 [P] [US2] Add `Thread.currentThread().interrupt();` before throwing in both `catch (InterruptedException e)` blocks in `src/main/java/com/biopatternsg/infrastructure/external_services/impl/QueryMeshImpl.java`

**Checkpoint**: User Story 2 is complete.

---

## Phase 5: User Story 3 - Null-Safe Domain Collections & Explicit `toString()` Across Models and DTOs (Priority: P3)

**Goal**: Add `@ToString` to `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult`, and `ESummaryResult`, and make `GeneOntologyBuildTreeRequest#getGoTermIds()` null-safe.

**Independent Test**: `GeneOntologyBuildTreeRequestTest` passes for null/overlapping lists and `toString()` assertions.

### Implementation for User Story 3

- [X] T012 [P] [US3] Add `@ToString` and update `getGoTermIds()` to filter `Objects::nonNull` before `flatMap(List::stream)` and use `.distinct().toList()` in `src/main/java/com/biopatternsg/domain/model/GeneOntologyBuildTreeRequest.java`
- [X] T013 [P] [US3] Add `@ToString` to `MeshInfo` in `src/main/java/com/biopatternsg/domain/model/mesh/MeshInfo.java`
- [X] T014 [P] [US3] Add `@ToString` to `ESearchResult` and `ESearchResult.IdListWrapper` in `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESearchResult.java`
- [X] T015 [P] [US3] Add `@ToString` to `ESummaryResult`, `ESummaryResult.DocSum`, and `ESummaryResult.Item` in `src/main/java/com/biopatternsg/infrastructure/external_services/dto/mesh/ESummaryResult.java`
- [X] T016 [US3] Add unit tests for `GeneOntologyBuildTreeRequest#getGoTermIds()` (null lists, null elements, duplicates) and `toString()` in `src/test/java/com/biopatternsg/domain/model/GeneOntologyBuildTreeRequestTest.java` (depends on T012)

**Checkpoint**: User Story 3 is complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Run full verification gates from `quickstart.md`

- [X] T017 Run full validation gates (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test` and static `grep` checks for `Collectors.toList()` and `@Inject` field injections in `application.*` / `infrastructure.adapters.*`) per `specs/004-clean-code-static-mappers/quickstart.md`

---

## Dependencies & Execution Order

- **Phase 1 (`T001`)** -> **Phase 3 (`US1`), Phase 4 (`US2`), Phase 5 (`US3`)** can run in parallel -> **Phase 6 (`T017`)**.
- Within **US1**: `T002` and `T003` in parallel -> `T004`, `T005`, `T006`, `T007`, `T008` in parallel.
- Within **US2**: `T009`, `T010`, `T011` all in parallel.
- Within **US3**: `T012`, `T013`, `T014`, `T015` in parallel -> `T016`.

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete `T001`–`T008` (`US1`) and validate with `./mvnw test`.

### Incremental Delivery
1. Deliver **US1** (static mappers + adapter cleanup).
2. Deliver **US2** (constructor injection + interrupt hygiene).
3. Deliver **US3** (null-safe `GeneOntologyBuildTreeRequest` + `@ToString`).
4. Run **Phase 6** full verification.
