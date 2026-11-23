# Research: Inbound and Outbound Adapters Delivery Layer Compliance

**Feature**: `003-adapters-delivery-compliance`  
**Date**: 2026-11-23  
**Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/003-adapters-delivery-compliance/spec.md)

## 1. Decisions & Rationale

### Decision 1: Request & Response DTO Records in `com.biopatternsg.infrastructure.dtos`
- **Decision**: Create three Java `record` DTOs in `com.biopatternsg.infrastructure.dtos`:
  - `BuildGeneOntologyTreeRequest(List<String> cellularComponent, List<String> molecularFunction, List<String> biologicalProcess)`
  - `BuildMeshTreeRequest(String id, String name, String symbol, List<String> synonyms)`
  - `MessageResponse(String message)`
- **Rationale**: Matches the existing Java `record` convention in `com.biopatternsg.infrastructure.dtos` (`CheckMeshTypeRequest`, `SearchMeshIdRequest`, `MeshIdResponse`, `UpdateMeshIdRequest`) and satisfies Constitutional Principles III & IV.
- **Alternatives Considered**: Using Lombok `@Data` classes. Rejected because Java `record`s are already the standard for `infrastructure.dtos.*` and provide built-in `toString()`, immutability, and Jackson serialization.

### Decision 2: Stateless Static Mappers in `com.biopatternsg.infrastructure.mapper`
- **Decision**: Create `OntologyRequestMapper` (`public final` utility class with private constructor and `public static` null-safe methods `requestToModel(BuildGeneOntologyTreeRequest)` and `requestToModel(BuildMeshTreeRequest)`).
- **Rationale**: Strictly complies with Constitutional Principle V (stateless pure static mappers, zero CDI annotations, standardized `requestToModel` naming, null-safe).
- **Alternatives Considered**: Adding methods to `GoTermMapper` (which is `@ApplicationScoped`). Rejected because Principle V mandates new mappers in `infrastructure.mapper.*` be `public final` static utility classes with zero CDI annotations.

### Decision 3: Single-Expression Lambdas & Helper Extraction in Controllers
- **Decision**:
  - In `GeneOntologyController`: extract `private void runBuildGeneOntologyTree(GeneOntologyBuildTreeRequest request)` so `CompletableFuture.runAsync(() -> runBuildGeneOntologyTree(domainRequest), executor)` is a single-expression lambda.
  - In `MeshOntologyController`: extract `private Response buildMeshNotFoundResponse()` (used via `this::buildMeshNotFoundResponse` in `.orElseGet(...)`) and `private void runSendBiologicalObjectToQueue(BiologicalObject biologicalObject)` (used in `CompletableFuture.runAsync(...)`).
  - Update `@APIResponse` `@Schema(implementation = MessageResponse.class)` on `202` and `404` responses, and fix `"Buildind mesh tree"` -> `"Building mesh tree"`.
- **Rationale**: Eliminates all multi-line lambdas and raw JSON string concatenation in controllers while preserving exact HTTP status codes and JSON payloads.

### Decision 4: `BiologicalObjectsHttpClient` Method Rename & Log Fix
- **Decision**: Rename `BiologicalObjectsHttpClient#buildMeshOntologyTree` to `updateMeshId`, update `QueryBiologicalObjectsImpl` to call `biologicalObjectsHttpClient.updateMeshId(...)`, and fix the log message to `"Error updating meshId for biological object {}: {}"`.
- **Rationale**: Aligns the method name with `@PATCH @Path("biological-object/update-mesh-id")` and removes the duplicated `"mesh MeshId"` typo.
