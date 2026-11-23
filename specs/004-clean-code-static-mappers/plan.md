# Implementation Plan: Clean Code Lambdas, Dependency Injection, Null Safety and Static Mappers

**Branch**: `004-clean-code-static-mappers` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/004-clean-code-static-mappers/spec.md)

**Input**: Feature specification from `/specs/004-clean-code-static-mappers/spec.md`

## Summary

Standardize mappers, dependency injection, null safety, `@ToString`, and interrupt hygiene across `services/ontologies`:
1. Refactor `GoTermMapper` to a `public final` static utility class (`quickGoToModelList`, `collectionToModel`, `modelToCollection`, `pathsToRootToModel`), using `.toList()`, `List.of()`, and single-expression helper calls; update `GOAdapter` and `GeneOntologyTermRepositoryAdapter`.
2. Create `MeshTermMapper` (`public final` static utility class) and extract `toCaseInsensitiveFilters` in `MeshOntologyRepositoryAdapter`.
3. Standardize constructor injection in `MeshQueueSenderRabbitAdapter` and `QueryGoImpl`.
4. Add `@ToString` to `GeneOntologyBuildTreeRequest`, `MeshInfo`, `ESearchResult`, and `ESummaryResult`; make `GeneOntologyBuildTreeRequest#getGoTermIds()` null-safe; restore `Thread.currentThread().interrupt()` in `QueryMeshImpl`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`)  
**Primary Dependencies**: Quarkus 3.24.2, Lombok 1.18.34, SmallRye Reactive Messaging RabbitMQ, MongoDB Panache  
**Storage**: MongoDB (`gene_ontology`, `mesh_ontology`) — schemas untouched  
**Testing**: JUnit 5 (`quarkus-junit5`), ArchUnit 1.4.1 (`ArchitectureLayerTest`)  
**Target Platform**: Linux server (Quarkus microservice)  
**Project Type**: Microservice (`services/ontologies`)  
**Performance Goals**: Eliminate CDI proxy overhead on mappers; single-pass `.distinct().toList()` in `GeneOntologyBuildTreeRequest`  
**Constraints**: Preserve all external/internal API behaviors and Apache 2.0 license headers  
**Scale/Scope**: 10 main files + unit tests

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I — Hexagonal Layer Isolation**: PASS.
- **Principle II — Use Cases & Application Services Encapsulation**: PASS.
- **Principle III — Zero Business Logic in Inbound & Outbound Adapters**: PASS.
- **Principle IV — Java 21 Clean Code & Method Discipline**: PASS. Constructor injection, `.toList()`, single-expression lambdas, null-safe collections (`List.of()`), `@ToString`, interrupt restoration.
- **Principle V — Stateless Pure Mappers, Naming Conventions & Use Case Documentation**: PASS. `GoTermMapper` and `MeshTermMapper` are `public final` static utility classes with private constructors and standardized method names.

*Post-Design Re-Check*: All 5 principles satisfied with zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/004-clean-code-static-mappers/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── mapper-and-di-contracts.md
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/java/com/biopatternsg/
│   ├── domain/model/
│   │   ├── GeneOntologyBuildTreeRequest.java                # @ToString + null-safe getGoTermIds()
│   │   └── mesh/MeshInfo.java                               # @ToString
│   └── infrastructure/
│       ├── adapters/out/
│       │   ├── GeneOntologyTermRepositoryAdapter.java       # Use static GoTermMapper
│       │   ├── MeshOntologyRepositoryAdapter.java           # Use static MeshTermMapper + toCaseInsensitiveFilters
│       │   ├── external_repositories/GOAdapter.java         # Use static GoTermMapper
│       │   └── producers/MeshQueueSenderRabbitAdapter.java  # Explicit constructor injection
│       ├── external_services/
│       │   ├── dto/mesh/
│       │   │   ├── ESearchResult.java                       # @ToString
│       │   │   └── ESummaryResult.java                      # @ToString
│       │   └── impl/
│       │       ├── QueryGoImpl.java                         # Remove redundant @RestClient @Inject on field
│       │       └── QueryMeshImpl.java                       # Restore Thread.currentThread().interrupt()
│       └── mapper/
│           ├── GoTermMapper.java                            # Static utility class
│           └── MeshTermMapper.java                          # New static utility class
└── test/java/com/biopatternsg/
    ├── domain/model/GeneOntologyBuildTreeRequestTest.java   # New unit test
    └── infrastructure/mapper/
        ├── GoTermMapperTest.java                            # Updated for static methods
        └── MeshTermMapperTest.java                          # New unit test
```

**Structure Decision**: Single-module Quarkus Maven project (`services/ontologies`).

## Complexity Tracking

> No Constitution Check violations; table not required.
