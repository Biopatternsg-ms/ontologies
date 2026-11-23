# Research: Architecture and Documentation Governance Automated Tests

**Feature**: `005-arch-doc-governance`  
**Date**: 2026-11-23  
**Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/ontologies/specs/005-arch-doc-governance/spec.md)

## 1. Decisions & Rationale

### Decision 1: Extending `ArchitectureLayerTest.java` with All 5 Constitutional ArchUnit Rules
- **Decision**: Update `src/test/java/archtest/ArchitectureLayerTest.java` to include:
  1. `domainShouldNotDependOnFrameworksTest`: forbids `com.biopatternsg.domain..` from depending on `jakarta.ws.rs..`, `jakarta.enterprise..`, `jakarta.inject..`, `jakarta.xml.bind..`, `com.fasterxml.jackson..`, `io.quarkus..`, `io.vertx..`, `org.bson..`, or `org.eclipse.microprofile..` (plus `org.w3c.dom..` already checked).
  2. `applicationShouldNotDependOnDeliveryOrPersistenceFrameworksTest`: forbids `com.biopatternsg.application..` from depending on `jakarta.ws.rs..`, `io.vertx..`, `io.quarkus.mongodb..`, or `org.bson..`.
  3. `useCasesShouldNotDependOnServiceImplementationsTest`: forbids `com.biopatternsg.application.usecase..` from depending on `com.biopatternsg.application.services.impl..`.
  4. `staticMappersShouldBeStatelessFinalUtilityClassesTest`: verifies top-level classes in `..infrastructure.mapper..` are `final` (`Modifier.FINAL`), have only `private` constructors, and are not annotated with `@ApplicationScoped`, `@Singleton`, or `@RequestScoped`.
  5. `noFieldInjectionInApplicationAndAdaptersTest`: verifies no fields in `com.biopatternsg.application..` or `com.biopatternsg.infrastructure.adapters..` are annotated with `@Inject`.
- **Rationale**: Replaces the temporary `domainLayerFrameworkPurityTest` / `applicationLayerFrameworkPurityTest` names from `001` with the full, exact governance rules from `REQ-ONT-ARCH-05` while covering every forbidden package and modifier.

### Decision 2: `DocumentationGovernanceTest.java` Implementation
- **Decision**: Create `src/test/java/archtest/DocumentationGovernanceTest.java` using standard Java NIO (`Files.list`, `Files.readString`) to scan `src/main/java/com/biopatternsg/application/usecase/*UseCase.java` and `.agents/use_cases/`:
  1. Verify every `*UseCase.java` has `.agents/use_cases/<UseCaseName>.md`.
  2. Verify each `<UseCaseName>.md` contains `sequenceDiagram`.
  3. Verify each `<UseCaseName>.md` is referenced in `.agents/use_cases/README.md`.
  4. Verify each `<UseCaseName>.md` is referenced in Section 5 of `.agents/architecture.md` (substring between `## 5. Casos de Uso Principales` and `## 6.`).
  5. Verify no orphan `.md` files (excluding `README.md`) exist in `.agents/use_cases/` without a matching `*UseCase.java`.
- **Rationale**: Zero external dependencies, deterministic, fast (< 20ms), and catches both missing and orphaned documentation.

### Decision 3: Updating Verification Gates in Documentation & Constitution
- **Decision**: Update `.agents/architecture-rules.md` (§6.2), `.agents/architecture.md` (§8), and `.specify/memory/constitution.md` (`Development Workflow & Quality Gates`, with Sync Impact Report and version bump `1.0.0` -> `1.0.1`) to include `./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest`.
- **Rationale**: Keeps all governance documents synchronized with the automated test suite.
