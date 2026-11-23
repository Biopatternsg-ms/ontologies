# Research: Use Cases Lightweight Orchestration and Application Services Encapsulation

**Feature**: `002-usecase-services-encapsulation`  
**Date**: 2026-11-23  
**Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/002-usecase-services-encapsulation/spec.md)

## 1. Decisions & Rationale

### Decision 1: Shared `resolveEffectiveName(BiologicalObject)` Helper
- **Decision**: Introduce a stateless `public final` utility class `BiologicalObjectNameResolver` in `com.biopatternsg.application.services` with `public static Optional<String> resolveEffectiveName(BiologicalObject biologicalObject)`.
- **Rationale**: Both `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase` previously duplicated the `name` -> `symbol.trim()` fallback and mutated `biologicalObject.setName(...)`. Placing `resolveEffectiveName` in `application.services.BiologicalObjectNameResolver` returns `Optional<String>` without mutating `BiologicalObject` and without altering `SendBiologicalObjectToQueueUseCase` constructor dependencies.
- **Alternatives Considered**: Adding `resolveEffectiveName` to `MeshOntologyService` (requires injecting `MeshOntologyService` into `SendBiologicalObjectToQueueUseCase`, which currently only needs `MeshOntologyRepository` and `MeshQueueSender`).

### Decision 2: `BuildMeshOntologyTreeUseCase` Pre-Check, `ArrayDeque`, Typo Fix & Decomposition
- **Decision**:
  - Resolve `Optional<String> effectiveNameOpt = BiologicalObjectNameResolver.resolveEffectiveName(biologicalObject);` at the start of `execute(...)`.
  - Check `isSavedName(effectiveName)` **before** calling `meshOntologyService.getMeshTermIds(effectiveName)` or `getSummarySynonyms(...)`, and remove the redundant `isSavedName(name)` check inside the `findFirstMatchTerm` loop.
  - Replace `java.util.Stack<String>` with `Deque<String> termIdsQueue = new ArrayDeque<>()`.
  - Rename `saveMechTerm` to `saveMeshTerm` (or call `meshOntologyRepository.save(...)` directly).
  - Extract ancestor node building (`buildAncestorMeshInfo(String termId, SummaryMesh summary)`) and tree traversal (`saveAncestorTree(MeshInfo rootMatch)`) into small single-responsibility `private` methods.
  - Change `findFirstMatchTerm`, `isSavedName`, and `isSavedTerm` from package-private to `private`.
- **Rationale**: Prevents unnecessary external NCBI calls when the term is already persisted, replaces legacy synchronized `Stack`, fixes the typo, and keeps `execute(...)` concise and readable.

### Decision 3: Deduplicating BFS Traversal in `CheckMeshTermTypeUseCase`
- **Decision**: Extract a shared private BFS traversal method `traverseAncestors(String startId, MeshInfo startTerm, Function<MeshInfo, Boolean> shouldContinue)` and a private parent-enqueuing helper `enqueueParents(MeshInfo term, Queue<String> queue, Set<String> visited)`.
- **Rationale**: `execute(...)` and `executeAll(...)` previously duplicated the entire `ArrayDeque` + `HashSet` BFS loop and parent-filtering logic (6 levels of nesting). Using `traverseAncestors` with `shouldContinue` lets `execute(...)` stop immediately on first match while `executeAll(...)` stops when `pendingCategories.isEmpty()`, with shallow nesting and zero duplicated code.

### Decision 4: Cleaning `BuildGeneOntologyTreeUseCase` & `SendBiologicalObjectToQueueUseCase`
- **Decision**:
  - In `BuildGeneOntologyTreeUseCase.getNewGOTermIds(...)`, collect `termIdsInDB` into a `Set<String>` (`Collectors.toSet()`) for `O(1)` lookup.
  - In `BuildGeneOntologyTreeUseCase.execute(...)`, replace the multi-line `.forEach(goTerm -> { ... })` lambda with a `for` loop calling a private `enrichAndSaveGoTerm(GoTerm goTerm)` method.
  - In `SendBiologicalObjectToQueueUseCase`, change `isSavedName` to `private` and use `effectiveName` without mutating `biologicalObject`.
- **Rationale**: Eliminates `O(n)` list containment checks and side-effectful multi-line lambdas per Constitutional Principle IV.

### Decision 5: `BiologicalObjectsServiceImpl` Rename & Strict `MeshOntologyRepository` Contract
- **Decision**:
  - Rename `BiologicalObjectsServiceImp` to `BiologicalObjectsServiceImpl`.
  - Remove `default {}` bodies from `updateCategories(String meshId, Map<String, Boolean> categories)` and `updateCategory(String meshId, String category, boolean isType)` in `MeshOntologyRepository`.
  - Add explicit `@Override` stubs for `updateCategories` and `updateCategory` in `SearchMeshIdBySynonymsUseCaseTest.StubMeshOntologyRepository`.
- **Rationale**: Enforces consistent `*Impl` naming across all application services and prevents adapters or test doubles from silently ignoring category persistence.
