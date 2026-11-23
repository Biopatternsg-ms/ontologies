# Implementation Plan: Inbound and Outbound Adapters Delivery Layer Compliance

**Branch**: `003-adapters-delivery-compliance` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/003-adapters-delivery-compliance/spec.md)

**Input**: Feature specification from `/specs/003-adapters-delivery-compliance/spec.md`

## Summary

Bring REST controllers and internal HTTP client adapters into full compliance with Constitutional Principles III, IV, and V:
1. Introduce Java `record` DTOs (`BuildGeneOntologyTreeRequest`, `BuildMeshTreeRequest`, `MessageResponse`) in `com.biopatternsg.infrastructure.dtos`.
2. Introduce a stateless static mapper `OntologyRequestMapper` in `com.biopatternsg.infrastructure.mapper`.
3. Update `GeneOntologyController` and `MeshOntologyController` to bind Request DTOs, return `MessageResponse` (fixing `"Buildind mesh tree"` -> `"Building mesh tree"`), update `@Schema(implementation = MessageResponse.class)`, and extract private helpers so `CompletableFuture.runAsync(...)` and `.orElseGet(...)` use single-expression lambdas or method references.
4. Rename `BiologicalObjectsHttpClient#buildMeshOntologyTree` to `updateMeshId` and fix the log typo in `QueryBiologicalObjectsImpl`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`)  
**Primary Dependencies**: Quarkus 3.24.2, Quarkus REST Jackson, Jakarta Bean Validation, MicroProfile OpenAPI & REST Client  
**Storage**: N/A (delivery/client layer only)  
**Testing**: JUnit 5 (`quarkus-junit5`), ArchUnit 1.4.1 (`ArchitectureLayerTest`)  
**Target Platform**: Linux server (Quarkus microservice)  
**Project Type**: Microservice (`services/ontologies`)  
**Performance Goals**: Zero runtime overhead; lightweight record DTOs and static mapping  
**Constraints**: Preserve all REST paths, HTTP status codes, and external JSON field names; maintain Apache 2.0 license headers  
**Scale/Scope**: 3 new DTO records, 1 static mapper, 4 updated main classes, and unit tests

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I — Hexagonal Layer Isolation**: PASS. Controllers no longer bind `domain.model.*` as HTTP request bodies; they depend on `infrastructure.dtos.*`, `infrastructure.mapper.*`, and `domain.port.in.*`.
- **Principle II — Use Cases & Application Services Encapsulation**: PASS. Controllers invoke only Inbound Ports.
- **Principle III — Zero Business Logic in Inbound & Outbound Adapters**: PASS. Controllers only validate DTOs, map to domain models, invoke Inbound Ports, and return Response DTOs.
- **Principle IV — Java 21 Clean Code & Method Discipline**: PASS. Java `record` DTOs (`toString()` built in), single-expression lambdas / method references, private helpers.
- **Principle V — Stateless Pure Mappers, Naming Conventions & Use Case Documentation**: PASS. `OntologyRequestMapper` is `public final` with private constructor, `public static requestToModel(...)` methods, zero CDI annotations.

*Post-Design Re-Check*: All 5 principles satisfied with zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/003-adapters-delivery-compliance/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── rest-and-client-contracts.md
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/java/com/biopatternsg/infrastructure/
│   ├── dtos/
│   │   ├── BuildGeneOntologyTreeRequest.java                # New record
│   │   ├── BuildMeshTreeRequest.java                        # New record
│   │   └── MessageResponse.java                             # New record
│   ├── mapper/
│   │   └── OntologyRequestMapper.java                       # New static mapper
│   ├── adapters/in/restcontrollers/
│   │   ├── GeneOntologyController.java                      # Bind DTO, MessageResponse, private helper
│   │   └── MeshOntologyController.java                      # Bind DTO, MessageResponse, private helpers, fix typo
│   ├── clients/internal_clients/
│   │   └── BiologicalObjectsHttpClient.java                 # Rename buildMeshOntologyTree -> updateMeshId
│   └── internal_services/impl/
│       └── QueryBiologicalObjectsImpl.java                  # Call updateMeshId, fix log typo
└── test/java/com/biopatternsg/infrastructure/
    ├── adapters/in/restcontrollers/
    │   ├── MeshOntologyControllerTest.java                  # Update 404 assertion to MessageResponse & add buildTreeMesh test
    │   └── GeneOntologyControllerTest.java                  # New unit test for 202 + MessageResponse
    └── mapper/
        └── OntologyRequestMapperTest.java                   # New unit test for static mapper
```

**Structure Decision**: Single-module Quarkus Maven project (`services/ontologies`).

## Complexity Tracking

> No Constitution Check violations; table not required.
