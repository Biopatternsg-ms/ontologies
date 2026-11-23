# Research: Clean Code Lambdas, Dependency Injection, Null Safety and Static Mappers

**Feature**: `004-clean-code-static-mappers`  
**Date**: 2026-11-23  
**Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/004-clean-code-static-mappers/spec.md)

## 1. Decisions & Rationale

### Decision 1: Refactoring `GoTermMapper` to a Stateless Static Utility Class
- **Decision**: Convert `GoTermMapper` to `public final class GoTermMapper` with a `private GoTermMapper()` constructor, zero `@ApplicationScoped` / CDI annotations, and `public static` null-safe methods:
  - `quickGoToModelList(GoTermResponse response)`
  - `collectionToModel(GoTermCollection entity)`
  - `modelToCollection(GoTerm model)`
  - `pathsToRootToModel(PathsToRootResponse response)`
- **Rationale**: Eliminates CDI bean overhead, resolves the conflicting `toEntity` method overloads, replaces all `Collectors.toList()` calls with `.toList()`, returns `List.of()` for null collection inputs, and replaces multi-line builder lambdas with clean single-expression helper calls (`mapGoTermToModel`, `mapSynonymToCollection`, `mapParentRelationToCollection`, etc.).
- **Callers Updated**: `GOAdapter` and `GeneOntologyTermRepositoryAdapter` drop their `private final GoTermMapper` fields and call `GoTermMapper.*` statically. `GoTermMapperTest` is updated to invoke static methods directly.

### Decision 2: Creating `MeshTermMapper` & Cleaning `MeshOntologyRepositoryAdapter`
- **Decision**: Create `public final class MeshTermMapper` in `com.biopatternsg.infrastructure.mapper` with `private MeshTermMapper()`, `public static MeshInfo collectionToModel(MeshTermCollection entity)`, and `public static MeshTermCollection modelToCollection(MeshInfo model)` (returning `List.of()` for null `synonyms`/`parents`). Remove inline `toModel` / `toEntity` from `MeshOntologyRepositoryAdapter` and extract `private Stream<Bson> toCaseInsensitiveFilters(String synonym)` for `.flatMap(this::toCaseInsensitiveFilters)`.
- **Rationale**: Centralizes MeSH persistence mapping in `infrastructure.mapper` and removes the multi-line `.flatMap(s -> { ... })` lambda.

### Decision 3: Standardizing Constructor Injection
- **Decision**:
  - In `MeshQueueSenderRabbitAdapter`: remove `@RequiredArgsConstructor` and `@Inject` field injection; declare `private final Emitter<BiologicalObject> emitter;` and an explicit constructor `public MeshQueueSenderRabbitAdapter(@Channel("ncbi-out") Emitter<BiologicalObject> emitter)`.
  - In `QueryGoImpl`: remove redundant `@RestClient` and `@Inject` annotations from `private final GeneOntologyHttpClient goHttpClient;` (already constructor-injected with `@RestClient`).
- **Rationale**: Enforces clean constructor injection across all adapters and services.

### Decision 4: Null-Safe `GeneOntologyBuildTreeRequest`, `@ToString`, and Interrupt Restoration
- **Decision**:
  - In `GeneOntologyBuildTreeRequest`: add `@ToString` and update `getGoTermIds()` to `Stream.of(cellularComponent, molecularFunction, biologicalProcess).filter(Objects::nonNull).flatMap(List::stream).filter(Objects::nonNull).distinct().toList()`.
  - Add `@ToString` to `MeshInfo`, `ESearchResult` (and `IdListWrapper`), and `ESummaryResult` (and `DocSum`, `Item`).
  - In `QueryMeshImpl`: call `Thread.currentThread().interrupt();` in both `catch (InterruptedException e)` blocks before throwing `IllegalStateException` / `RuntimeException`.
- **Rationale**: Prevents `NullPointerException` on partial GO requests, ensures explicit `toString()` on all domain models and DTOs, and preserves thread interruption signals.
