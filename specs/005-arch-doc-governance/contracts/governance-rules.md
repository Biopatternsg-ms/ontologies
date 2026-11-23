# Governance Verification Contracts

**Feature**: `005-arch-doc-governance`  
**Date**: 2026-11-23

## 1. `ArchitectureLayerTest` Rules (`src/test/java/archtest/ArchitectureLayerTest.java`)
- `domainShouldNotDependOnFrameworksTest`
- `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`
- `useCasesShouldNotDependOnServiceImplementationsTest`
- `staticMappersShouldBeStatelessFinalUtilityClassesTest`
- `noFieldInjectionInApplicationAndAdaptersTest`

## 2. `DocumentationGovernanceTest` Rules (`src/test/java/archtest/DocumentationGovernanceTest.java`)
- Bidirectional 1:1 mapping between `application/usecase/*UseCase.java` and `.agents/use_cases/*.md` (excluding `README.md`).
- Mermaid `sequenceDiagram` presence in every Use Case doc.
- Index references in `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`.
