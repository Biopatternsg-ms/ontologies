# Implementation Plan: Architecture and Documentation Governance Automated Tests

**Branch**: `005-arch-doc-governance` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/005-arch-doc-governance/spec.md)

**Input**: Feature specification from `/specs/005-arch-doc-governance/spec.md`

## Summary

Automate architectural and documentation governance in `services/ontologies`:
1. Extend `ArchitectureLayerTest.java` with the 5 ArchUnit rules (`domainShouldNotDependOnFrameworksTest`, `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`, `useCasesShouldNotDependOnServiceImplementationsTest`, `staticMappersShouldBeStatelessFinalUtilityClassesTest`, `noFieldInjectionInApplicationAndAdaptersTest`).
2. Create `DocumentationGovernanceTest.java` verifying 1:1 Use Case <-> `.agents/use_cases/<UseCaseName>.md` synchronization, Mermaid `sequenceDiagram`, and indexing in `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`.
3. Update verification gate commands in `.agents/architecture-rules.md`, `.agents/architecture.md`, and `.specify/memory/constitution.md` (`1.0.0` -> `1.0.1`).

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`)  
**Primary Dependencies**: ArchUnit 1.4.1 (`archunit-junit5`), JUnit 5 (`quarkus-junit5`), Java NIO (`java.nio.file.*`)  
**Storage**: N/A (file-system & bytecode verification tests)  
**Testing**: JUnit 5 + ArchUnit  
**Target Platform**: Linux / CI Maven runner  
**Project Type**: Microservice (`services/ontologies`)  
**Performance Goals**: Sub-5s execution for governance test suite  
**Constraints**: Apache 2.0 license headers on new Java test files; constitution amendment procedure followed (`1.0.1`)  
**Scale/Scope**: 2 test files + 3 governance markdown files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principles I–V**: PASS. This feature directly automates verification of Principles I, II, IV, and V.
- **Governance / Amendment Procedure**: Updating `.specify/memory/constitution.md` to add `DocumentationGovernanceTest` to Development Workflow & Quality Gates follows the `PATCH` amendment procedure (Sync Impact Report + version `1.0.1`).

*Post-Design Re-Check*: All principles and governance rules satisfied.

## Project Structure

### Documentation (this feature)

```text
specs/005-arch-doc-governance/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── governance-rules.md
└── tasks.md
```

### Source Code (repository root)

```text
src/test/java/archtest/
├── ArchitectureLayerTest.java           # Extend with 5 governance ArchUnit rules
└── DocumentationGovernanceTest.java     # New Use Case documentation governance test

.agents/
├── architecture-rules.md                # Add DocumentationGovernanceTest to §6.2
└── architecture.md                      # Add DocumentationGovernanceTest to §8

.specify/memory/
└── constitution.md                      # Add DocumentationGovernanceTest to Quality Gates (v1.0.1)
```

**Structure Decision**: Single-module Quarkus Maven project (`services/ontologies`).

## Complexity Tracking

> No Constitution Check violations; table not required.
