# Research: Domain Layer Purity and Framework Decoupling

**Feature**: `001-domain-layer-purity`  
**Date**: 2026-11-23  
**Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/001-domain-layer-purity/spec.md)

## 1. Decisions & Rationale

### Decision 1: `BiologicalObjectsRepository.updateMeshId` Return Type & Response Lifecycle
- **Decision**: Change `BiologicalObjectsRepository.updateMeshId(String biologicalObjectId, String meshId)` and `BiologicalObjectsService.updateMeshId(String biologicalObjectId, String meshId)` to return `void`. Move `try (Response response = queryBiologicalObjects.updateMeshId(...))` and HTTP status logging into `BiologicalObjectsAdapter`.
- **Rationale**: `BiologicalObjectsServiceImp` previously opened `try (Response response = ...)` solely to log `response.getStatus()` and close the JAX-RS `Response`. Neither the domain nor the application layer branches on the HTTP status code. Moving `try-with-resources` and status logging to `BiologicalObjectsAdapter` removes `jakarta.ws.rs.core.Response` from both `domain` and `application` while preserving identical runtime logging and resource cleanup.
- **Alternatives Considered**: Returning a custom `UpdateResult(int statusCode)` or `boolean` from `BiologicalObjectsRepository`. Rejected because no caller in `application` inspects the status code; `void` keeps the port minimal and avoids unused return values.

### Decision 2: Removal of `MeshQueueConsumer` from `domain.port.out.consumers`
- **Decision**: Delete `com.biopatternsg.domain.port.out.consumers.MeshQueueConsumer` and remove `implements MeshQueueConsumer` from `MeshQueueConsumerRabbitImpl`.
- **Rationale**: `MeshQueueConsumer` is an inbound messaging adapter concern (SmallRye `@Incoming("ncbi-in")` receiving `io.vertx.core.json.JsonObject`), not an outbound domain port. `MeshQueueConsumerRabbitImpl` already injects and invokes the inbound port `com.biopatternsg.domain.port.in.BuildMeshOntologyTree`. Removing the interface eliminates `io.vertx.core.json.JsonObject` from `domain` with zero impact on RabbitMQ consumption.
- **Alternatives Considered**: Changing `MeshQueueConsumer` to accept `BiologicalObject` and moving it to `domain.port.in`. Rejected because `BuildMeshOntologyTree` is already the dedicated inbound port for this flow; a second inbound port would be redundant.

### Decision 3: Relocation & Cleanup of NCBI MeSH XML DTOs (`ESearchResult`, `ESummaryResult`)
- **Decision**: Move `ESearchResult` and `ESummaryResult` from `com.biopatternsg.domain.model.mesh` to `com.biopatternsg.infrastructure.external_services.dto.mesh`. Update imports in `MeshOntologyHttpClient` and `QueryMeshImpl`. In `ESummaryResult.Item`:
  - Remove unused `getSubItemValues()` (which also had an NPE risk on `element.getFirstChild().getNodeValue()`).
  - Rename `getValuesBySubItemNameOtra(String subItemName)` to `getValuesBySubItemNameOrText(String subItemName)`.
  - Add null guards for `content == null`, `child.getTextContent() == null`, and `element.getTextContent() == null` in both `getValuesBySubItemName` and `getValuesBySubItemNameOrText`.
- **Rationale**: These classes exist solely to deserialize NCBI E-utilities XML responses via JAXB (`jakarta.xml.bind.annotation.*`), Jackson (`@JsonIgnoreProperties`), Quarkus reflection (`@RegisterForReflection`), and `org.w3c.dom.Element`. No domain or application class references them. Cleaning up `Item` prevents NPEs on empty XML elements and removes Spanglish naming.
- **Alternatives Considered**: Creating separate domain models for `ESearchResult`/`ESummaryResult` and mapping them. Rejected because `QueryMeshImpl` already extracts `List<String>` and `SummaryMesh` (the actual domain model) directly from these DTOs.

### Decision 4: Decoupling `PathsToRoot` and `PathToRoot` from Jackson
- **Decision**:
  - Remove `@JsonIgnoreProperties(ignoreUnknown = true)` and `com.fasterxml.jackson.annotation.JsonIgnoreProperties` from `com.biopatternsg.domain.model.PathsToRoot` and `com.biopatternsg.domain.model.PathToRoot`.
  - Create Java `record` DTOs `PathsToRootResponse` and `PathToRootResponse` (annotated with `@JsonIgnoreProperties(ignoreUnknown = true)`) in `com.biopatternsg.infrastructure.external_services.dto.go`.
  - Update `GeneOntologyHttpClient`, `QueryGO`, and `QueryGoImpl` to return `PathsToRootResponse`.
  - Add a null-safe mapping method `toPathsToRoot(PathsToRootResponse response)` in `GoTermMapper` and invoke it from `GOAdapter.getPathsToRoot(String goId)`.
- **Rationale**: `GeneOntologyHttpClient` already uses `GoTermResponse` (a Java `record` in `infrastructure.external_services.dto.go`) mapped via `GoTermMapper` in `GOAdapter.findGOTerms(...)`. Applying the exact same pattern to `getPathsToRoot(...)` keeps `domain.model` 100% free of Jackson annotations and aligns with Constitutional Principle IV (Java `record` DTOs).
- **Alternatives Considered**: Using Jackson mix-ins in infrastructure to keep `PathsToRoot` deserializable without annotations. Rejected because mix-ins add hidden configuration complexity whereas `GoTermResponse` + `GoTermMapper` is already the established pattern in this codebase.

### Decision 5: Strengthening ArchUnit Rules in `ArchitectureLayerTest`
- **Decision**: Verify existing `ArchitectureLayerTest` rules and add explicit ArchUnit checks (or verify via `ArchitectureLayerTest`) ensuring `domain..` does not depend on `jakarta.ws.rs..`, `io.vertx..`, `io.quarkus..`, `jakarta.xml.bind..`, `com.fasterxml.jackson..`, `org.bson..`, or `org.w3c.dom..`, and `application..` does not depend on `jakarta.ws.rs..` or `io.vertx..`.
- **Rationale**: Prevents future regressions of Constitutional Principle I automatically during `./mvnw test`.
- **Alternatives Considered**: Relying only on manual code review. Rejected because ArchUnit is already configured in `src/test/java/archtest/ArchitectureLayerTest.java` and runs on every `./mvnw test`.
