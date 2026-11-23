# Data Model: Architecture and Documentation Governance Automated Tests

**Feature**: `005-arch-doc-governance`  
**Date**: 2026-11-23

> No production domain models or database schemas are modified. This feature adds automated test suites and updates governance documentation.

## 1. Governance Artifacts Verified

- **Use Case Source Files**: `src/main/java/com/biopatternsg/application/usecase/*UseCase.java`
- **Use Case Docs**: `.agents/use_cases/<UseCaseName>.md` (must contain `sequenceDiagram`)
- **Catalog Index**: `.agents/use_cases/README.md`
- **Architecture Section 5**: `.agents/architecture.md` (between `## 5. Casos de Uso Principales` and `## 6.`)
