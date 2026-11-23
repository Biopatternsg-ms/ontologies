# Feature Specification: Clean Code Lambdas, Dependency Injection, Null Safety and Static Mappers

**Feature Branch**: `004-clean-code-lambdas-di-mappers`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/ontologies/04-clean-code-lambdas-dependency-injection-and-static-mappers"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Stateless Static Mappers & Pure Stream Transformations (Priority: P1)

As a service maintainer, I want all data mappers (`GoTermMapper`, `MeshTermMapper`) to be stateless utility classes with deterministic `public static` null-safe methods, modern immutable list collection (`.toList()`), and single-expression lambdas so that mapping logic is reusable, predictable, and free of CDI lifecycle overhead.

**Why this priority**: `GoTermMapper` is currently a CDI bean with conflicting `toEntity` overloads, legacy `Collectors.toList()`, multi-line builder lambdas, and `null` collection returns, while `MeshOntologyRepositoryAdapter` embeds inline mapping methods and a multi-line filter lambda.

**Independent Test**: Can be verified by unit-testing `GoTermMapper` and `MeshTermMapper` static methods with null, empty, and populated inputs, confirming zero CDI annotations on mappers, zero `Collectors.toList()` or multi-line lambdas in mappers/adapters, and passing `GOAdapter` / `GeneOntologyTermRepositoryAdapter` / `MeshOntologyRepositoryAdapter` compilation and tests.

**Acceptance Scenarios**:

1. **Given** a null or populated QuickGO response, MongoDB collection entity, or domain model, **When** `GoTermMapper` converts between representations using standardized static methods (`quickGoToModelList`, `collectionToModel`, `modelToCollection`, `pathsToRootToModel`), **Then** null collection inputs return empty lists (`List.of()`), zero multi-line lambdas or `Collectors.toList()` calls exist, and callers (`GOAdapter`, `GeneOntologyTermRepositoryAdapter`) invoke static methods without injecting `GoTermMapper`.
2. **Given** a `MeshTermCollection` or `MeshInfo` domain model, **When** `MeshOntologyRepositoryAdapter` maps between persistence and domain models, **Then** it delegates to static methods on `MeshTermMapper` (`collectionToModel`, `modelToCollection`), and `findBySynonymsCaseInsensitive` uses a clean method reference (`.flatMap(this::toCaseInsensitiveFilters)`) instead of a multi-line lambda.

---

### User Story 2 - Constructor Injection Hygiene & Thread Interrupt Restoration (Priority: P2)

As a service maintainer, I want all adapters and external service implementations to use clean constructor injection without redundant field-level `@Inject` annotations, and external query sleep handlers to restore thread interrupt status on `InterruptedException` so that dependency wiring is explicit and thread cancellation signals are never swallowed.

**Why this priority**: Field-level `@Inject` alongside unused `@RequiredArgsConstructor` (`MeshQueueSenderRabbitAdapter`) or redundant `@RestClient @Inject` on a constructor-injected `final` field (`QueryGoImpl`) obscures dependency injection, and swallowing `InterruptedException` in `QueryMeshImpl` breaks cooperative thread cancellation.

**Independent Test**: Can be verified by inspecting DI annotations across `application.*`, `infrastructure.adapters.*`, and `QueryGoImpl`, and verifying `QueryMeshImpl` restores `Thread.currentThread().interrupt()` when `InterruptedException` occurs.

**Acceptance Scenarios**:

1. **Given** `MeshQueueSenderRabbitAdapter` and `QueryGoImpl`, **When** their dependency injection is inspected, **Then** both use clean `private final` constructor injection with zero field-level `@Inject` or redundant `@RestClient` annotations.
2. **Given** a thread executing `QueryMeshImpl#getTermIds` or `#getSummarySynonyms` is interrupted during `Thread.sleep`, **When** `InterruptedException` is caught, **Then** `Thread.currentThread().interrupt()` is called before throwing.

---

### User Story 3 - Null-Safe Domain Collections & Explicit `toString()` Across Models and DTOs (Priority: P3)

As a service maintainer, I want domain models and relocated external DTOs to define explicit `toString()` representations and `GeneOntologyBuildTreeRequest#getGoTermIds()` to safely handle `null` category lists so that logging/debugging is clear and partial Gene Ontology requests never cause `NullPointerException`.

**Why this priority**: Missing `@ToString` on `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult`, and `ESummaryResult` degrades observability, and `GeneOntologyBuildTreeRequest#getGoTermIds()` throws `NullPointerException` if any of its three lists is `null`.

**Independent Test**: Can be verified by unit-testing `GeneOntologyBuildTreeRequest#getGoTermIds()` with null and overlapping lists, and verifying explicit `toString()` output on `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult`, and `ESummaryResult`.

**Acceptance Scenarios**:

1. **Given** a `GeneOntologyBuildTreeRequest` where one or more of `cellularComponent`, `molecularFunction`, or `biologicalProcess` is `null`, **When** `getGoTermIds()` is called, **Then** `null` lists are safely skipped without throwing `NullPointerException` and distinct GO term IDs are returned.
2. **Given** `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult`, and `ESummaryResult` (including nested classes), **When** `toString()` is invoked, **Then** an explicit, informative string representation is produced via `@ToString`.

---

### Edge Cases

- What happens when `GoTermMapper` or `MeshTermMapper` receives a `null` entity or `null` nested list? `null` top-level objects return `null` (or empty `PathsToRoot` / `List.of()` for collection/list methods), and `null` nested lists return `List.of()`.
- What happens when `GeneOntologyBuildTreeRequest` has all three lists `null`? `getGoTermIds()` returns an empty `List.of()` without throwing `NullPointerException`.
- What happens when `QueryMeshImpl` catches `InterruptedException`? The interrupt flag is restored (`Thread.currentThread().interrupt()`) before rethrowing.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `GoTermMapper` MUST be refactored to a `public final` utility class with a `private` constructor, zero CDI annotations, `public static` null-safe methods (`quickGoToModelList`, `collectionToModel`, `modelToCollection`, `pathsToRootToModel`), `.toList()` instead of `Collectors.toList()`, empty `List.of()` for null collection inputs, and zero multi-line lambdas.
- **FR-002**: `MeshTermMapper` MUST be created as a `public final` utility class in `com.biopatternsg.infrastructure.mapper` with a `private` constructor and `public static` null-safe methods (`collectionToModel`, `modelToCollection`), replacing inline private mapping methods in `MeshOntologyRepositoryAdapter`.
- **FR-003**: `GeneOntologyTermRepositoryAdapter` and `GOAdapter` MUST remove injected `GoTermMapper` fields and invoke `GoTermMapper` static methods directly.
- **FR-004**: `MeshOntologyRepositoryAdapter#findBySynonymsCaseInsensitive` MUST extract `private Stream<Bson> toCaseInsensitiveFilters(String synonym)` so `.flatMap(this::toCaseInsensitiveFilters)` is a single-expression method reference.
- **FR-005**: `MeshQueueSenderRabbitAdapter` MUST use `private final Emitter<BiologicalObject> emitter;` with an explicit constructor (`@Channel("ncbi-out")`) and remove unused `@RequiredArgsConstructor` and field-level `@Inject`.
- **FR-006**: `QueryGoImpl` MUST remove redundant field-level `@RestClient` and `@Inject` annotations from its constructor-injected `private final` field.
- **FR-007**: `GeneOntologyBuildTreeRequest#getGoTermIds()` MUST filter `Objects::nonNull` before `flatMap(List::stream)` and use `.distinct().toList()`.
- **FR-008**: `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult` (and `IdListWrapper`), and `ESummaryResult` (and `DocSum`, `Item`) MUST define explicit `@ToString`.
- **FR-009**: `QueryMeshImpl` MUST call `Thread.currentThread().interrupt()` before throwing when catching `InterruptedException`.

### Key Entities

- **`GoTermMapper` & `MeshTermMapper`**: Stateless pure static mapper utility classes in `com.biopatternsg.infrastructure.mapper`.
- **`GeneOntologyBuildTreeRequest` & `MeshInfo`**: Domain models with explicit `@ToString` and null-safe GO term ID aggregation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0 CDI annotations or instance methods remain on `GoTermMapper` and `MeshTermMapper`.
- **SC-002**: 0 `Collectors.toList()` calls or multi-line lambdas remain in `GoTermMapper` or `MeshOntologyRepositoryAdapter`.
- **SC-003**: 0 `@Inject` field injections remain in `application.*` or `infrastructure.adapters.*`, and 0 redundant `@RestClient @Inject` field annotations remain in `QueryGoImpl`.
- **SC-004**: 100% of `domain.model.*` and `infrastructure.dtos.*` classes define explicit `toString()`, and `./mvnw clean test` passes with zero errors.

## Assumptions

- `GoTermMapper` method renames (`quickGoToModelList`, `collectionToModel`, `modelToCollection`, `pathsToRootToModel`) will be updated across all callers (`GOAdapter`, `GeneOntologyTermRepositoryAdapter`, and `GoTermMapperTest`).
- Third-party mapping frameworks (MapStruct, ModelMapper) and resilience libraries (SmallRye Fault Tolerance / Resilience4j) remain out of scope.
