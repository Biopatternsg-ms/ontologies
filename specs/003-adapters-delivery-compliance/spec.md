# Feature Specification: Inbound and Outbound Adapters Delivery Layer Compliance

**Feature Branch**: `003-adapters-delivery-compliance`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/ontologies/03-inbound-and-outbound-adapters-delivery-layer-compliance"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Typed Request & Response DTOs in REST Controllers (Priority: P1)

As an API consumer and service maintainer, I want REST controllers to accept dedicated typed Request DTOs instead of binding domain models directly and to return typed Response DTOs instead of hardcoded JSON strings so that HTTP contracts are decoupled from domain models and typos in response payloads are eliminated.

**Why this priority**: Binding domain models directly in HTTP endpoints couples external API schemas to internal domain structures, and raw hardcoded JSON strings bypass type safety and OpenAPI schema generation (already causing the `"Buildind mesh tree"` typo).

**Independent Test**: Can be verified by sending HTTP requests to the Gene Ontology and MeSH tree-building and search endpoints, confirming identical JSON field names, HTTP status codes (`202`, `404`), and corrected `"Building mesh tree"` message, while verifying no controller binds `domain.model.*` request bodies.

**Acceptance Scenarios**:

1. **Given** a client submits a Gene Ontology tree-building request (`cellularComponent`, `molecularFunction`, `biologicalProcess`), **When** the endpoint receives and validates the payload, **Then** it binds a dedicated Request DTO record, maps it to the domain model via a stateless mapper, and returns a `202 Accepted` response with a typed `MessageResponse` (`"Building Gene Ontology tree"`).
2. **Given** a client submits a MeSH tree-building request (`id`, `name`, `symbol`, `synonyms`), **When** the endpoint receives and validates the payload, **Then** it binds a dedicated Request DTO record, maps it to `BiologicalObject` via a stateless mapper, and returns a `202 Accepted` response with a typed `MessageResponse` (`"Building mesh tree"`).
3. **Given** a client searches for a MeSH ID by synonyms that yields no match, **When** the endpoint returns `404 Not Found`, **Then** the response body is a typed `MessageResponse` (`"MeSH term not found for provided synonyms"`) rather than a raw hardcoded JSON string.

---

### User Story 2 - Single-Expression Lambdas in Controller Async & Optional Flows (Priority: P2)

As a service maintainer, I want asynchronous execution and optional fallback flows in REST controllers to use clean single-expression lambdas or method references backed by private helper methods so that controller endpoints remain concise, readable, and compliant with clean-code standards.

**Why this priority**: Multi-line `try/catch` blocks inside `CompletableFuture.runAsync(...)` and multi-line `.orElseGet(...)` builders clutter controller endpoints and violate Constitutional Principle IV.

**Independent Test**: Can be verified by inspecting both controllers to confirm zero multi-line lambdas remain and running controller tests (`MeshOntologyControllerTest`) and unit/architecture tests.

**Acceptance Scenarios**:

1. **Given** an asynchronous `202 Accepted` endpoint in either controller, **When** background execution is scheduled via `CompletableFuture.runAsync(...)`, **Then** the lambda delegates to a single-responsibility private helper method (`runBuildGeneOntologyTree(...)`, `runSendBiologicalObjectToQueue(...)`) that encapsulates error logging.
2. **Given** an optional lookup result in `MeshOntologyController`, **When** the fallback response is built via `.orElseGet(...)`, **Then** it uses a method reference or single-expression helper (`this::buildMeshNotFoundResponse`).

---

### User Story 3 - Accurate Internal HTTP Client Naming & Clean Consumer Adapter (Priority: P3)

As a service maintainer, I want internal HTTP client method names and error logs to accurately reflect the `PATCH /biological-object/update-mesh-id` operation and the RabbitMQ inbound consumer to remain a clean standalone adapter delegating strictly to `BuildMeshOntologyTree` so that internal integration code is unambiguous.

**Why this priority**: Misleading method names (`buildMeshOntologyTree` for a `PATCH update-mesh-id` call) and duplicated words in error logs (`"Error updating mesh MeshId..."`) create confusion during maintenance and incident debugging.

**Independent Test**: Can be verified by confirming `BiologicalObjectsHttpClient#updateMeshId` is used by `QueryBiologicalObjectsImpl` with the corrected log message, `MeshQueueConsumerRabbitImpl` delegates cleanly to `BuildMeshOntologyTree`, and `./mvnw test` passes.

**Acceptance Scenarios**:

1. **Given** the internal biological objects HTTP client and its service wrapper, **When** updating a biological object's MeSH ID, **Then** the client method is named `updateMeshId` and error logs read `"Error updating meshId for biological object {}: {}"`.
2. **Given** the inbound RabbitMQ consumer (`MeshQueueConsumerRabbitImpl`), **When** a message arrives on `ncbi-in`, **Then** it deserializes `BiologicalObject` cleanly and delegates strictly to `BuildMeshOntologyTree.execute(...)`.

---

### Edge Cases

- What happens when an incoming `BuildGeneOntologyTreeRequest` or `BuildMeshTreeRequest` violates Bean Validation constraints? The JAX-RS/Quarkus validation layer rejects the request with `400 Bad Request` before invoking any mapper or use case.
- What happens when `CompletableFuture.runAsync(...)` encounters an exception during background execution? The extracted private helper (`runBuildGeneOntologyTree` / `runSendBiologicalObjectToQueue`) catches and logs the exception safely without crashing the executor thread.
- What happens when OpenAPI documentation is generated for `202` and `404` responses? `@Schema(implementation = MessageResponse.class)` accurately describes the `{"message": "..."}` JSON schema.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `GeneOntologyController` MUST bind a typed Java `record` Request DTO (`BuildGeneOntologyTreeRequest` with `cellularComponent`, `molecularFunction`, `biologicalProcess` and Bean Validation constraints matching the domain model) and map it to `GeneOntologyBuildTreeRequest` via a stateless static mapper (`public final` utility class with `public static` methods).
- **FR-002**: `MeshOntologyController` MUST bind a typed Java `record` Request DTO (`BuildMeshTreeRequest` with `id`, `name`, `symbol`, `synonyms` and Bean Validation constraints matching `BiologicalObject`) and map it to `BiologicalObject` via a stateless static mapper.
- **FR-003**: A typed Java `record` `MessageResponse(String message)` MUST be introduced in `com.biopatternsg.infrastructure.dtos` and used instead of raw hardcoded JSON strings in `GeneOntologyController#buildTreeGO` (`"Building Gene Ontology tree"`), `MeshOntologyController#searchMeshId` (`"MeSH term not found for provided synonyms"`), and `MeshOntologyController#buildTreeMesh` (`"Building mesh tree"`, fixing the `"Buildind"` typo), with OpenAPI `@Schema(implementation = MessageResponse.class)` updated accordingly.
- **FR-004**: Multi-line lambdas in `GeneOntologyController` and `MeshOntologyController` (`CompletableFuture.runAsync(...)` and `.orElseGet(...)`) MUST be replaced with single-expression lambdas or method references delegating to private helpers (`runBuildGeneOntologyTree(...)`, `runSendBiologicalObjectToQueue(...)`, `buildMeshNotFoundResponse()`).
- **FR-005**: `BiologicalObjectsHttpClient#buildMeshOntologyTree` MUST be renamed to `updateMeshId`, and `QueryBiologicalObjectsImpl` MUST update its caller and fix the `"Error updating mesh MeshId..."` log message typo.
- **FR-006**: `MeshQueueConsumerRabbitImpl` MUST remain a clean standalone inbound Quarkus RabbitMQ adapter delegating strictly to `BuildMeshOntologyTree`.
- **FR-007**: Existing REST URL paths, HTTP status codes (`200`, `202`, `400`, `404`), and external JSON field names (`cellularComponent`, `molecularFunction`, `biologicalProcess`, `id`, `name`, `symbol`, `synonyms`, `message`) MUST remain unchanged.

### Key Entities

- **`BuildGeneOntologyTreeRequest`**: Infrastructure Request DTO record representing Gene Ontology build-tree input categories.
- **`BuildMeshTreeRequest`**: Infrastructure Request DTO record representing a biological object submitted for MeSH tree construction.
- **`MessageResponse`**: Infrastructure Response DTO record (`record MessageResponse(String message)`) representing standardized JSON message payloads.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0 REST controller endpoints bind `domain.model.*` classes directly as HTTP request bodies.
- **SC-002**: 0 raw hardcoded JSON message strings or typos (`"Buildind"`) remain in REST controller responses.
- **SC-003**: 0 multi-line lambdas exist inside `GeneOntologyController` or `MeshOntologyController`.
- **SC-004**: 100% of existing and new controller, mapper, and architecture tests (`MeshOntologyControllerTest`, `ArchitectureLayerTest`) pass with zero errors.

## Assumptions

- Existing Bean Validation annotations on `GeneOntologyBuildTreeRequest` and `BiologicalObject` will be mirrored on `BuildGeneOntologyTreeRequest` and `BuildMeshTreeRequest` so `400 Bad Request` validation behavior remains identical.
- Static mappers for the new Request DTOs will follow Constitutional Principle V (`public final` utility class with private constructor and `public static` null-safe methods).
- `MeshQueueConsumer` removal from `domain.port.out.consumers` was already completed in `001-domain-layer-purity`, so FR-006 verifies `MeshQueueConsumerRabbitImpl` remains clean and compliant.
