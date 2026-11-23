# Feature Specification: Domain Layer Purity and Framework Decoupling

**Feature Branch**: `001-domain-layer-purity`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/ontologies/01-domain-layer-purity-and-framework-decoupling"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Pure Domain Boundary for External Service & Messaging Ports (Priority: P1)

As a service maintainer, I want all domain outbound ports and application services to be completely free of HTTP response types and message-broker payload types so that core business orchestration does not depend on transport or delivery mechanisms.

**Why this priority**: Transport types (`BiologicalObjectsRepository` returning an HTTP response and `MeshQueueConsumer` placed as an outbound port with a broker JSON type) directly couple both the domain and application layers to delivery frameworks, violating Constitutional Principle I (Hexagonal Layer Isolation).

**Independent Test**: Can be verified by inspecting imports across the domain and application layers, confirming that updating a biological object's MeSH identifier and consuming MeSH queue messages operate without transport-specific types in domain or application contracts, and running the build and architecture test suite.

**Acceptance Scenarios**:

1. **Given** the biological objects outbound port and its application service caller, **When** an update to a biological object's MeSH identifier is requested, **Then** the domain port and application service exchange only pure domain/primitive types, while HTTP response lifecycle management and status handling occur solely in the outbound adapter.
2. **Given** the inbound MeSH queue consumer adapter, **When** a queue message arrives to trigger MeSH ontology tree construction, **Then** the adapter invokes the inbound use-case port directly without relying on any outbound consumer port or broker-specific JSON types in the domain layer.

---

### User Story 2 - Decoupled External Ontology Serialization Models (Priority: P2)

As a service maintainer, I want external ontology serialization DTOs (XML/JAXB structures for NCBI MeSH and JSON-annotated structures for Gene Ontology) to live exclusively in the infrastructure layer so that domain models represent pure business concepts without serialization, reflection, or XML DOM parsing concerns.

**Why this priority**: Domain models (`ESearchResult`, `ESummaryResult`, `PathsToRoot`, `PathToRoot`) currently embed serialization annotations, runtime reflection hints, and XML DOM traversal logic, making the domain fragile to external wire-format changes.

**Independent Test**: Can be verified by confirming that external XML DTOs reside in the infrastructure external-services DTO package with clean, null-safe DOM helpers, that domain models for ontology paths contain zero serialization annotations, and that all ontology queries and mappings pass unit and architecture tests.

**Acceptance Scenarios**:

1. **Given** an external NCBI E-utilities XML search or summary response, **When** the infrastructure client deserializes and extracts MeSH data, **Then** XML/JAXB annotations, reflection hints, and null-safe DOM traversal helpers reside exclusively in infrastructure DTOs rather than domain models.
2. **Given** a Gene Ontology paths-to-root response from the external QuickGO service, **When** the infrastructure adapter retrieves and converts the response, **Then** deserialization uses dedicated infrastructure response DTOs mapped into annotation-free domain models.

---

### User Story 3 - Clean & Null-Safe External Summary Extraction (Priority: P3)

As a service maintainer, I want the relocated MeSH summary extraction helpers to use clear English naming, remove unused dead methods, and guard against missing XML child nodes so that external MeSH metadata parsing is reliable and maintainable.

**Why this priority**: The existing XML summary extraction logic contains unused methods with null-pointer risks and inconsistent Spanglish naming; cleaning it up alongside relocation prevents runtime failures and improves code clarity.

**Independent Test**: Can be verified by reviewing and unit-testing the relocated summary extraction methods with complete, partial, and missing XML child nodes to ensure no null-pointer exceptions occur and no dead methods remain.

**Acceptance Scenarios**:

1. **Given** an external MeSH summary XML item with missing or empty child nodes, **When** sub-item values are extracted, **Then** null child nodes are safely handled without throwing null-pointer exceptions.
2. **Given** the relocated MeSH summary DTO, **When** its public methods are inspected, **Then** unused dead extraction methods are removed and all remaining methods use clear, consistent English names.

---

### Edge Cases

- What happens when an external biological objects update returns a non-2xx HTTP status or fails during response handling? The outbound adapter safely closes the response resource and logs or handles the status without exposing transport types to the application or domain layers.
- What happens when an external MeSH XML summary item has a null `firstChild` or empty sub-item list? The extraction helper guards against null nodes and returns safe empty/default values instead of throwing a `NullPointerException`.
- What happens when an external Gene Ontology paths-to-root response contains unknown JSON fields? The infrastructure response DTO ignores unknown properties during deserialization while keeping the mapped domain model free of serialization annotations.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The domain layer MUST NOT import or reference web/HTTP (`jakarta.ws.rs.*`), reactive messaging/broker (`io.vertx.*`), runtime reflection (`io.quarkus.*`), XML binding (`jakarta.xml.bind.*`), JSON serialization (`com.fasterxml.jackson.*`), or persistence (`org.bson.*`) packages.
- **FR-002**: The application layer MUST NOT import or reference web/HTTP (`jakarta.ws.rs.*`) or reactive messaging/broker (`io.vertx.*`) packages.
- **FR-003**: The biological objects outbound port (`BiologicalObjectsRepository`) MUST NOT return a transport-specific HTTP response type; it MUST return `void` (or a pure domain/Java result) while delegating HTTP response inspection and resource closing (`try-with-resources`) to its infrastructure outbound adapter (`BiologicalObjectsAdapter`).
- **FR-004**: The misplaced outbound consumer port (`MeshQueueConsumer`) MUST be removed from the domain layer, and the inbound RabbitMQ consumer (`MeshQueueConsumerRabbitImpl`) MUST operate as a standalone inbound adapter invoking the `BuildMeshOntologyTree` inbound port.
- **FR-005**: External NCBI MeSH XML serialization models (`ESearchResult`, `ESummaryResult`) MUST be relocated from the domain layer to the infrastructure external-services DTO package, and all referencing clients and query services MUST be updated accordingly.
- **FR-006**: The relocated `ESummaryResult.Item` helper MUST remove the unused `getSubItemValues()` method, rename `getValuesBySubItemNameOtra` to a clear English name, and guard against `null` XML child nodes.
- **FR-007**: Domain models `PathsToRoot` and `PathToRoot` MUST be stripped of all JSON serialization annotations and imports; dedicated infrastructure response DTO records MUST be introduced for external QuickGO deserialization and mapped to domain models in `GOAdapter` via `GoTermMapper`.
- **FR-008**: All existing external HTTP/XML wire contracts (`ncbi-api`, `ebi-api`, `biological-objects-api`) and RabbitMQ exchange, queue, and channel names (`exchange-ncbi`, `mesh-queue`, `ncbi-in`, `ncbi-out`) MUST remain unchanged.

### Key Entities

- **Biological Object Update Port**: Domain contract representing the action of associating a MeSH identifier with a biological object without exposing transport response details.
- **External MeSH XML DTOs (`ESearchResult`, `ESummaryResult`)**: Infrastructure-layer serialization structures representing NCBI E-utilities search and summary XML responses.
- **Gene Ontology Path Models (`PathsToRoot`, `PathToRoot`) & Infrastructure DTOs**: Pure domain models representing ontology hierarchy paths to root, paired with infrastructure response records for external JSON deserialization.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0 files in the domain layer contain imports from web, messaging, XML/JAXB, JSON serialization, persistence, or runtime framework packages.
- **SC-002**: 0 files in the application layer contain imports from web/HTTP or messaging/broker packages.
- **SC-003**: 100% of external XML/JSON deserialization models for MeSH and Gene Ontology reside in the infrastructure layer with pure mapping to domain models.
- **SC-004**: 100% of existing compilation, architecture boundary verification, and automated test suites pass with zero errors after the decoupling changes.

## Assumptions

- Returning `void` from `BiologicalObjectsRepository.updateMeshId(String biologicalObjectId, String meshId)` is the preferred contract, with `BiologicalObjectsAdapter` responsible for closing the JAX-RS `Response` via `try-with-resources` and logging non-success statuses; if a boolean/status indicator is needed later, it can be introduced as a pure Java type without changing the hexagonal boundary.
- Existing RabbitMQ consumer behavior (`MeshQueueConsumerRabbitImpl`) already deserializes or reads the incoming message and delegates to `BuildMeshOntologyTree`, so removing `MeshQueueConsumer` does not alter runtime message consumption.
- No external API or broker contract changes are permitted; all modifications are strictly internal refactoring to restore hexagonal layer purity.
