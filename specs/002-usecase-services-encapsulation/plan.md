# Implementation Plan: Use Cases Lightweight Orchestration and Application Services Encapsulation

**Branch**: `002-usecase-services-encapsulation` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/002-usecase-services-encapsulation/spec.md)

**Input**: Feature specification from `/specs/002-usecase-services-encapsulation/spec.md`

## Summary

Refactor the application use cases and service/repository contracts to restore lightweight orchestration, eliminate duplicated logic and input mutation, and enforce strict naming and interface contracts:
1. Centralize non-mutating `BiologicalObject` name resolution in `BiologicalObjectNameResolver.resolveEffectiveName(BiologicalObject)` and reuse it in `BuildMeshOntologyTreeUseCase` and `SendBiologicalObjectToQueueUseCase`.
2. Refactor `BuildMeshOntologyTreeUseCase` to check `isSavedName` before NCBI queries, replace `Stack` with `ArrayDeque`, fix `saveMechTerm` -> `saveMeshTerm`, make helpers `private`, and extract `saveAncestorTree` / `buildAncestorMeshInfo`.
3. Deduplicate the BFS ancestor traversal in `CheckMeshTermTypeUseCase` via a shared `traverseAncestors` + `enqueueParents` helper.
4. Clean `BuildGeneOntologyTreeUseCase` (`Set<String>` for existing IDs, `for` loop + `enrichAndSaveGoTerm` helper).
5. Rename `BiologicalObjectsServiceImp` -> `BiologicalObjectsServiceImpl` and remove `default {}` bodies from `MeshOntologyRepository.updateCategories` / `updateCategory`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`)  
**Primary Dependencies**: Quarkus 3.24.2, Lombok 1.18.34, Jakarta CDI  
**Storage**: MongoDB (`gene_ontology`, `mesh_ontology`) — schemas untouched  
**Testing**: JUnit 5 (`quarkus-junit5`), ArchUnit 1.4.1 (`ArchitectureLayerTest`)  
**Target Platform**: Linux server (Quarkus microservice)  
**Project Type**: Microservice (`services/ontologies`)  
**Performance Goals**: `O(1)` `Set.contains` lookup in `BuildGeneOntologyTreeUseCase`; avoid redundant NCBI calls when MeSH name is already persisted  
**Constraints**: Preserve MeSH matching rules, `MeshCategory` definitions, and MongoDB schemas; maintain Apache 2.0 license headers  
**Scale/Scope**: Targeted refactoring across 6 production files and unit tests in `src/test/java`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I — Hexagonal Layer Isolation**: PASS. No cross-layer violations introduced; `BiologicalObjectNameResolver` lives in `application.services`, accessed only by `application`.
- **Principle II — Use Cases & Application Services Encapsulation**: PASS. Use cases become lightweight orchestrators with `private` helpers and shared name resolution.
- **Principle III — Zero Business Logic in Adapters**: PASS. Adapters remain thin; `MeshOntologyRepository` enforces explicit implementation of category updates.
- **Principle IV — Java 21 Clean Code & Method Discipline**: PASS. Shallow nesting in `CheckMeshTermTypeUseCase`, `ArrayDeque` instead of `Stack`, `Set<String>` instead of `List.contains`, single-responsibility private helpers, no multi-line side-effect lambdas.
- **Principle V — Stateless Pure Mappers, Naming Conventions & Use Case Documentation**: PASS. `BiologicalObjectsServiceImpl` aligns with `*Impl` naming; no new Use Cases added.

*Post-Design Re-Check*: All 5 principles satisfied with zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/002-usecase-services-encapsulation/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── internal-contracts.md
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
src/
├── main/java/com/biopatternsg/
│   ├── domain/port/out/repositories/
│   │   └── MeshOntologyRepository.java                      # Remove default {} on updateCategories/updateCategory
│   └── application/
│       ├── services/
│       │   ├── BiologicalObjectNameResolver.java            # New stateless name-resolution helper
│       │   └── impl/
│       │       └── BiologicalObjectsServiceImpl.java        # Renamed from BiologicalObjectsServiceImp
│       └── usecase/
│           ├── BuildMeshOntologyTreeUseCase.java            # Pre-check isSavedName, ArrayDeque, private helpers
│           ├── CheckMeshTermTypeUseCase.java                # Shared BFS traverseAncestors + enqueueParents
│           ├── SendBiologicalObjectToQueueUseCase.java      # Non-mutating name resolution, private isSavedName
│           └── BuildGeneOntologyTreeUseCase.java            # Set<String> lookup + enrichAndSaveGoTerm
└── test/java/com/biopatternsg/application/usecase/
    ├── CheckMeshTermTypeUseCaseTest.java                    # Existing tests pass
    ├── SearchMeshIdBySynonymsUseCaseTest.java               # Implement updateCategories/updateCategory in stub
    ├── BuildMeshOntologyTreeUseCaseTest.java                # New unit tests (non-mutation, pre-check)
    └── SendBiologicalObjectToQueueUseCaseTest.java          # New unit tests (non-mutation, symbol fallback)
```

**Structure Decision**: Single-module Quarkus Maven project (`services/ontologies`).

## Complexity Tracking

> No Constitution Check violations; table not required.
