# Feature Specification: Use Cases Lightweight Orchestration and Application Services Encapsulation

**Feature Branch**: `002-usecase-services-encapsulation`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/ontologies/02-use-cases-lightweight-orchestration-and-services-encapsulation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lightweight Use Case Orchestration & Non-Mutating Name Resolution (Priority: P1)

As a service maintainer, I want use cases to act as lightweight, non-mutating orchestrators that share a single biological object name-resolution helper and check local existence before invoking external ontology lookups so that redundant external calls and input side effects are eliminated.

**Why this priority**: Input mutation (`biologicalObject.setName(...)`), duplicated fallback logic between MeSH tree building and queue dispatch, and checking local persistence *after* calling external NCBI endpoints cause unnecessary external traffic, subtle side effects, and duplicated maintenance burden.

**Independent Test**: Can be verified by executing the MeSH tree-building and queue-dispatch use cases with biological objects that have a `name`, only a `symbol`, or already-persisted names, confirming the input object is never mutated, local existence is checked before external lookups, and all unit tests pass.

**Acceptance Scenarios**:

1. **Given** a biological object whose `name` is blank but whose `symbol` is present, **When** queue dispatch or MeSH tree construction executes, **Then** the effective display name is resolved from `symbol` without mutating the incoming `BiologicalObject` instance.
2. **Given** a biological object whose resolved effective name already exists locally, **When** MeSH tree construction executes, **Then** local existence is verified before making any external MeSH search or summary lookup calls.
3. **Given** an iterative MeSH ancestor traversal during tree construction, **When** ancestor terms are queued and persisted, **Then** the traversal uses a modern double-ended queue (`ArrayDeque`), clean private decomposition, and correctly named persistence helpers without package-private helper exposure.

---

### User Story 2 - Deduplicated MeSH Category Traversal & Clean GO Enrichment (Priority: P2)

As a service maintainer, I want duplicated MeSH ancestor BFS traversal logic consolidated into a single reusable helper and Gene Ontology tree enrichment cleaned of `O(n)` list lookups and multi-line side-effect lambdas so that cognitive complexity is reduced and performance/readability improve.

**Why this priority**: Duplicated 6-level-deep BFS loops in MeSH category classification and `O(n)` containment checks / multi-line lambdas in GO enrichment increase defect risk and violate clean-code standards.

**Independent Test**: Can be verified by running single-category and multi-category MeSH classification tests (`CheckMeshTermTypeUseCaseTest`) and verifying that GO tree enrichment produces identical results using `Set`-based lookups and clean private helper methods.

**Acceptance Scenarios**:

1. **Given** a MeSH node evaluated for either a single category or all remaining categories, **When** the classification use case traverses ancestor nodes, **Then** both flows share a single BFS traversal and category-matching helper with shallow nesting and identical classification outcomes.
2. **Given** a set of candidate Gene Ontology term identifiers to enrich and persist, **When** GO tree construction filters existing identifiers and enriches new terms, **Then** existing-ID lookup uses a `Set<String>` and enrichment/persistence is performed via a clean loop or single-responsibility private helper instead of a multi-line `.forEach` lambda.

---

### User Story 3 - Consistent Service Naming & Strict Repository Contracts (Priority: P3)

As a service maintainer, I want application service implementations to follow consistent `*Impl` naming and repository ports to enforce explicit implementation of category persistence methods so that incomplete adapters cannot silently drop updates.

**Why this priority**: Inconsistent naming (`BiologicalObjectsServiceImp`) and silent no-op `default` methods on `MeshOntologyRepository` weaken codebase consistency and allow unimplemented persistence methods to fail silently.

**Independent Test**: Can be verified by confirming `BiologicalObjectsServiceImpl` is properly named and injected, `MeshOntologyRepository` declares `updateCategories` and `updateCategory` as abstract methods (and all adapters/test stubs implement them), and the full build and test suite pass.

**Acceptance Scenarios**:

1. **Given** the biological objects application service implementation, **When** its class name is inspected across the codebase, **Then** it is named `BiologicalObjectsServiceImpl` consistently with all other application service implementations.
2. **Given** the `MeshOntologyRepository` outbound port, **When** its category update methods (`updateCategories`, `updateCategory`) are inspected, **Then** they are abstract interface methods without silent no-op `default` implementations, ensuring every repository adapter and test double explicitly implements or stubs them.

---

### Edge Cases

- What happens when a `BiologicalObject` has both `name` and `symbol` null or blank? Effective name resolution returns empty (`Optional.empty()`), and the use case safely exits or skips without mutating the object or calling external services.
- What happens when an existing unit test in `CheckMeshTermTypeUseCaseTest` or `SearchMeshIdBySynonymsUseCaseTest` relies on a test double of `MeshOntologyRepository`? Since `updateCategories` and `updateCategory` become abstract, any in-memory/stub implementation in tests must explicitly implement them.
- What happens if a unit test previously called a package-private helper method on a use case directly? Tests are updated to exercise the behavior through the public `execute(...)` contract.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase` MUST reuse a single shared biological object name-resolution helper (`resolveEffectiveName(BiologicalObject)` returning `Optional<String>`) and MUST NOT mutate the incoming `BiologicalObject` instance.
- **FR-002**: `BuildMeshOntologyTreeUseCase` MUST verify whether the resolved effective name is already saved locally *before* calling external MeSH term ID or summary lookup services.
- **FR-003**: `BuildMeshOntologyTreeUseCase` MUST replace legacy `java.util.Stack` with `Deque<String>` (`ArrayDeque`), fix the `saveMechTerm` method name typo to `saveMeshTerm` (or delegate directly), and decompose ancestor node building into small `private` methods (or delegate to `MeshOntologyService`).
- **FR-004**: `CheckMeshTermTypeUseCase` MUST eliminate the duplicated BFS ancestor-traversal and parent-enqueuing loops between `execute(...)` and `executeAll(...)` by extracting a shared helper or `MeshOntologyService` method with shallow nesting.
- **FR-005**: `BuildGeneOntologyTreeUseCase` MUST use a `Set<String>` for existing-ID filtering in `getNewGOTermIds(...)` and MUST replace the multi-line `.forEach` lambda in `execute(...)` with a clean loop or private helper method (`enrichAndSaveGoTerm`).
- **FR-006**: Package-private helper methods in `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase` MUST be made `private`, with unit tests exercising behavior through public use case contracts.
- **FR-007**: `BiologicalObjectsServiceImp` MUST be renamed to `BiologicalObjectsServiceImpl`.
- **FR-008**: `MeshOntologyRepository` MUST declare `updateCategories(String, Map<String, Boolean>)` and `updateCategory(String, String, boolean)` as abstract interface methods (removing silent no-op `default {}` bodies).
- **FR-009**: Existing MeSH matching algorithm rules, `MeshCategory` keyword/ID definitions, and MongoDB document schemas (`gene_ontology`, `mesh_ontology`) MUST remain unchanged.

### Key Entities

- **Effective Biological Object Name**: Resolved display name derived from `BiologicalObject.name` or fallback `BiologicalObject.symbol` without mutating the source object.
- **MeSH Ancestor Traversal State**: Queue-based (`ArrayDeque`) traversal state tracking visited MeSH IDs and parent relationships during tree construction and category classification.
- **MeshOntologyRepository Contract**: Outbound persistence port requiring explicit implementations of `updateCategories` and `updateCategory`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0 use cases mutate incoming `BiologicalObject` parameters during name resolution or tree/queue processing.
- **SC-002**: 0 duplicated BFS traversal loops exist in `CheckMeshTermTypeUseCase`, and 0 `java.util.Stack` usages or `saveMechTerm` typos remain in `BuildMeshOntologyTreeUseCase`.
- **SC-003**: 100% of application service implementations follow the `*ServiceImpl` naming convention, and 100% of `MeshOntologyRepository` methods are explicit non-default contracts.
- **SC-004**: 100% of existing and updated unit tests and architecture boundary tests (`ArchitectureLayerTest`) pass with zero failures.

## Assumptions

- Centralizing `resolveEffectiveName(BiologicalObject)` in `MeshOntologyService` (or a dedicated package-neutral/application helper on `MeshOntologyService`) is preferred because both `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase` already depend on `MeshOntologyService` (or we can check if `SendBiologicalObjectToQueueUseCase` depends on it during planning).
- All existing MeSH matching rules, `MeshCategory` definitions, and MongoDB schemas are preserved without behavioral regressions.
