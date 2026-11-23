# Implementation Plan: Domain Layer Purity and Framework Decoupling

**Branch**: `001-domain-layer-purity` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/001-domain-layer-purity/spec.md)

**Input**: Feature specification from `/specs/001-domain-layer-purity/spec.md`

## Summary

Decouple `com.biopatternsg.domain` and `com.biopatternsg.application` from transport, messaging, XML/JAXB, JSON serialization, and Quarkus runtime framework dependencies by:
1. Changing `BiologicalObjectsRepository.updateMeshId` to `void` and moving `try (Response response = ...)` handling into `BiologicalObjectsAdapter`.
2. Removing the misplaced `MeshQueueConsumer` interface from `domain.port.out.consumers` while keeping `MeshQueueConsumerRabbitImpl` as a standalone inbound adapter.
3. Relocating `ESearchResult` and `ESummaryResult` to `infrastructure.external_services.dto.mesh` with clean English method names and null-safe DOM handling.
4. Stripping Jackson annotations from `PathsToRoot` and `PathToRoot`, introducing `PathsToRootResponse` / `PathToRootResponse` records in `infrastructure.external_services.dto.go`, and mapping them in `GOAdapter` via `GoTermMapper`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`)  
**Primary Dependencies**: Quarkus 3.24.2, Lombok 1.18.34, Jakarta Bean Validation, Jackson, JAXB, SmallRye Reactive Messaging RabbitMQ, MongoDB Panache  
**Storage**: MongoDB (`gene_ontology`, `mesh_ontology`) — untouched by this refactoring  
**Testing**: JUnit 5 (`quarkus-junit5`), RestAssured, ArchUnit 1.4.1 (`ArchitectureLayerTest`)  
**Target Platform**: Linux server (Quarkus microservice)  
**Project Type**: Microservice (`services/ontologies`)  
**Performance Goals**: No runtime regression; pure in-memory DTO mapping for `PathsToRootResponse` -> `PathsToRoot`  
**Constraints**: Zero changes to external HTTP/XML/JSON wire contracts (`ncbi-api`, `ebi-api`, `biological-objects-api`) or RabbitMQ channels/queues (`exchange-ncbi`, `mesh-queue`, `ncbi-in`, `ncbi-out`); Apache 2.0 license headers preserved  
**Scale/Scope**: Targeted architectural refactoring across ~14 Java source files and test coverage in `src/test/java`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I — Hexagonal Layer Isolation (NON-NEGOTIABLE)**: PASS (and directly enforced by this feature). Domain will have zero imports from `jakarta.ws.rs.*`, `io.vertx.*`, `io.quarkus.*`, `jakarta.xml.bind.*`, `com.fasterxml.jackson.*`, `org.bson.*`, or `org.w3c.dom.*`. Application will have zero imports from `jakarta.ws.rs.*` or `io.vertx.*`.
- **Principle II — Use Cases & Application Services Encapsulation**: PASS. `MeshQueueConsumerRabbitImpl` continues to invoke `BuildMeshOntologyTree` inbound port; `BiologicalObjectsServiceImp` continues to coordinate `BiologicalObjectsRepository`.
- **Principle III — Zero Business Logic in Inbound & Outbound Adapters**: PASS. `BiologicalObjectsAdapter` handles `try-with-resources` on JAX-RS `Response`; `GOAdapter` delegates DTO-to-domain mapping to `GoTermMapper`.
- **Principle IV — Java 21 Clean Code & Method Discipline**: PASS. New `PathsToRootResponse` and `PathToRootResponse` use Java `record`s; `ESummaryResult.Item` removes dead `getSubItemValues()`, renames `getValuesBySubItemNameOtra` to `getValuesBySubItemNameOrText`, and adds null guards; `PathsToRoot` and `PathToRoot` retain `@Data` (explicit `toString()`).
- **Principle V — Stateless Pure Mappers, Naming Conventions & Use Case Documentation**: PASS. Existing `GoTermMapper` (`@ApplicationScoped`, stateless) gains deterministic null-safe `toPathsToRoot(PathsToRootResponse)` without breaking `GeneOntologyTermRepositoryAdapter`; ArchUnit naming conventions (`*UseCase`, `*Controller`, `*Adapter`) are preserved. No new Use Cases are added.

*Post-Design Re-Check*: All 5 principles remain satisfied with zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-domain-layer-purity/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── internal-ports-and-dtos.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/
├── main/java/com/biopatternsg/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── PathToRoot.java                              # Remove @JsonIgnoreProperties
│   │   │   ├── PathsToRoot.java                             # Remove @JsonIgnoreProperties
│   │   │   └── mesh/
│   │   │       ├── ESearchResult.java                       # Move -> infrastructure.external_services.dto.mesh
│   │   │       └── ESummaryResult.java                      # Move -> infrastructure.external_services.dto.mesh
│   │   └── port/out/
│   │       ├── consumers/MeshQueueConsumer.java             # Delete
│   │       └── external_repositories/BiologicalObjectsRepository.java  # Return void
│   ├── application/
│   │   └── services/impl/BiologicalObjectsServiceImp.java   # Remove Response handling
│   └── infrastructure/
│       ├── adapters/
│       │   ├── in/consumers/MeshQueueConsumerRabbitImpl.java # Remove implements MeshQueueConsumer
│       │   └── out/external_repositories/
│       │       ├── BiologicalObjectsAdapter.java            # Close Response with try-with-resources & log status
│       │       └── GOAdapter.java                           # Map PathsToRootResponse -> PathsToRoot
│       ├── clients/external_clients/
│       │   ├── GeneOntologyHttpClient.java                  # Return PathsToRootResponse
│       │   └── MeshOntologyHttpClient.java                  # Update ESearchResult/ESummaryResult imports
│       ├── external_services/
│       │   ├── QueryGO.java                                 # Return PathsToRootResponse
│       │   ├── dto/
│       │   │   ├── go/
│       │   │   │   ├── PathToRootResponse.java              # New record
│       │   │   │   └── PathsToRootResponse.java             # New record
│       │   │   └── mesh/
│       │   │       ├── ESearchResult.java                   # Relocated from domain.model.mesh
│       │   │       └── ESummaryResult.java                  # Relocated & cleaned
│       │   └── impl/
│       │       ├── QueryGoImpl.java                         # Return PathsToRootResponse
│       │       └── QueryMeshImpl.java                       # Update imports & getValuesBySubItemNameOrText()
│       └── mapper/
│           └── GoTermMapper.java                            # Add toPathsToRoot(PathsToRootResponse)
└── test/java/
    ├── archtest/ArchitectureLayerTest.java                  # Add/verify domain & application framework-isolation rules
    └── com/biopatternsg/...                                 # Unit tests for ESummaryResult.Item, GoTermMapper, etc.
```

**Structure Decision**: Single-module Quarkus Maven project (`services/ontologies`) following strict Hexagonal Architecture (`domain`, `application`, `infrastructure`).

## Complexity Tracking

> No Constitution Check violations; table not required.
