<!--
Sync Impact Report
- Version change: 1.1.0 → 1.1.1
- Modified sections:
  - Development Workflow & Quality Gates: Added DocumentationGovernanceTest to verification gate command (`./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest`)
- Follow-up TODOs: None
-->

# ontologies Constitution

## Core Principles

### I. Hexagonal Layer Isolation (NON-NEGOTIABLE)

The service MUST maintain strict Hexagonal Architecture (Ports & Adapters) boundaries across `com.biopatternsg.domain.*`, `com.biopatternsg.application.*`, and `com.biopatternsg.infrastructure.*`:
- **Domain (`domain.*`):** MUST remain a pure business core (`model`, `model.mesh`, `port.in`, `port.out`) and MUST NOT depend on `application..`, `infrastructure..`, or delivery/persistence/messaging frameworks (e.g., `jakarta.ws.rs.*`, MongoDB Panache, RabbitMQ/Vert.x).
- **Application (`application.*`):** MUST depend only on `domain..` and MUST NOT depend on `infrastructure..`.
- **Infrastructure (`infrastructure.*`):** MUST interact with Application exclusively through Inbound Ports (`domain.port.in.*`); direct access to `application..` or `application.usecase..` from outside Application is prohibited.

### II. Use Cases & Application Services Encapsulation

Every business interaction MUST enter through a dedicated Inbound Port (`domain.port.in.*`) implemented by a single-responsibility `*UseCase` in `application.usecase.*`:
- Use Cases MUST act purely as lightweight orchestrators: validate preconditions and idempotency rules, and coordinate Outbound Ports (`domain.port.out.*`) or Application Services (`application.services.*`).
- Multi-step ontology lookups, hierarchy traversal helpers, external query coordination, and cross-service notifications MUST reside in Application Services (`application.services.*` interfaces with `@ApplicationScoped` implementations in `application.services.impl.*`, never exposing internal helper methods publicly).
- Application Services MUST NEVER be injected directly into REST Controllers or RabbitMQ Consumers; all inbound adapters MUST depend strictly on Inbound Port interfaces.

### III. Zero Business Logic in Inbound & Outbound Adapters

Delivery, messaging, and persistence adapters MUST remain thin and free of domain rules:
- **REST Controllers (`infrastructure.adapters.in.restcontrollers.*`):** MUST only receive and validate HTTP requests (`@Valid` DTOs in `infrastructure.dtos.*`), invoke Inbound Ports (synchronously or via managed `Executor` + `CompletableFuture.runAsync(...)` for `202 Accepted` endpoints), and return mapped Response DTOs. Business branching, ontology traversal, and direct repository queries are prohibited in controllers.
- **Messaging Consumers (`infrastructure.adapters.in.consumers.*`):** RabbitMQ consumers (`@Incoming("ncbi-in")`, `@Blocking`) MUST only deserialize incoming queue messages into Domain Models (`BiologicalObject`), delegate execution exclusively to the corresponding Inbound Port (`BuildMeshOntologyTree`), and log transport/deserialization errors safely.
- **Outbound Adapters (`infrastructure.adapters.out.*`):** Repository adapters (`PanacheMongoRepository`), messaging producer adapters (`@Channel("ncbi-out")`), and external/internal service adapters MUST implement `domain.port.out.*` interfaces and convert MongoDB collections (`infrastructure.mongo.*`) or external client DTOs into Domain Models (`domain.model.*`) before returning to Application.

### IV. Java 21 Clean Code & Method Discipline

All Java code in `ontologies` MUST adhere to clean, low-complexity standards:
- **Maximum 3 Parameters:** No method in a Use Case, Application Service, or Adapter may accept more than 3 individual parameters; larger inputs MUST be encapsulated in a Java `record`, Domain Model, or Command/Query object.
- **Streams & Private Helpers:** Complex multi-step logic MUST be decomposed into small single-responsibility private methods. Stream/Optional lambdas MUST be single-expression calls or method references and MUST NOT embed multi-line builders, branching, or try/catch blocks. Prefer `.toList()`, Java `record` DTOs (`infrastructure.dtos.*`), and enhanced `switch` expressions for exhaustive enum handling (e.g., `MeshCategory`).
- **Injection & Null Safety:** Dependencies MUST prefer constructor injection (`private final` fields with `@RequiredArgsConstructor` or an explicit constructor). Use `Objects.equals(a, b)`, return empty collections/maps (`List.of()`, `Collections.emptyMap()`) instead of `null`, avoid dead statements, and close JAX-RS `Response` objects with `try-with-resources`.
- **Explicit `toString()`:** Every class in `domain.model.*` (including `domain.model.mesh.*`) and `infrastructure.dtos.*` MUST define an explicit `toString()` (via `@ToString`, `@Data`, or Java `record`), excluding sensitive fields with `@ToString.Exclude`.
- **Log Injection Prevention (CWE-117 / CodeQL):** Every user-provided, message-queue, or external untrusted value passed to logger calls (`log.info`, `log.warn`, `log.error`, `log.debug`) MUST be sanitized via `com.biopatternsg.shared.LogSanitizer.sanitize(value)` (stripping/replacing `\n` and `\r` characters) before logging. Logging raw unsanitized external inputs is strictly prohibited.

### V. Stateless Pure Mappers, Naming Conventions & Use Case Documentation

Mapping, structural naming, and documentation standards MUST be strictly enforced:
- **Static Mappers:** New or refactored mapper classes in `infrastructure.mapper.*` MUST be `public final` utility classes with private constructors, `public static` deterministic pure functions, zero mutable state, zero CDI annotations (`@ApplicationScoped`, `@Singleton`, `@RequestScoped`), and null-safe handling using standardized method names (`requestToModel`, `collectionToModel`, `modelToCollection`, `modelToResponse`, `<provider>ToModel`).
- **ArchUnit Naming Conventions:** Classes in `application.usecase..` MUST end with `UseCase`, classes in `infrastructure.adapters.in.restcontrollers..` MUST end with `Controller`, and classes in `infrastructure.adapters.out..` MUST end with `Adapter`.
- **Use Case Documentation:** Every new or modified Use Case MUST have English documentation in `.agents/use_cases/<UseCaseName>.md` (problem, contracts, step-by-step flow, Mermaid `sequenceDiagram`, and caching/failure scenarios) and MUST be indexed in both `.agents/use_cases/README.md` and Section 5 of `.agents/architecture.md`.

## Technology & Integration Constraints

- **Stack:** Java 21, Quarkus 3.24.2, Lombok, Jakarta Bean Validation, ArchUnit, MongoDB with Panache (`GoTermCollection` -> `gene_ontology`, `MeshTermCollection` -> `mesh_ontology`), and SmallRye Reactive Messaging RabbitMQ (`exchange-ncbi`, `mesh-queue`, channels `ncbi-out` / `ncbi-in`).
- **Internal Microservice Integrations:** Consumed via MicroProfile REST Clients and outbound adapters for `search-biological-objects` (`biological-objects-api`, `PATCH /biological-object/update-mesh-id` via `BiologicalObjectsAdapter`).
- **External Biological APIs:** Consumed via MicroProfile REST Clients and outbound adapters for EBI QuickGO (`ebi-api`, JSON via `GOAdapter`) and NCBI E-utilities MeSH (`ncbi-api`, XML/JAXB with `DoctypeRemovalFilter` via `MeshAdapter`).
- **Licensing:** Source files MUST maintain Apache 2.0 license headers as configured in `pom.xml` (`license-maven-plugin`).

## Development Workflow & Quality Gates

Before completing any feature, refactoring, or bug fix:
1. Run `./mvnw clean compile` to verify compilation and refresh `target/classes`.
2. Run `./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest` to verify all ArchUnit hexagonal layer/naming rules and Use Case documentation governance checks pass.
3. Run `./mvnw test` to confirm unit and controller tests pass without regressions.
4. Update `.agents/use_cases/<UseCaseName>.md`, `.agents/use_cases/README.md`, and `.agents/architecture.md` synchronously whenever Use Cases, Ports, or integrations are added or modified.

## Governance

This Constitution supersedes ad-hoc development practices in `ontologies`. Detailed architectural and coding standards are maintained in `.agents/architecture-rules.md`, `.agents/java-rules.md`, `.agents/architecture.md`, and `.agents/use_cases/README.md`.

- **Amendment Procedure:** Proposed changes to governance principles MUST update this constitution, include a Sync Impact Report for review, and synchronize affected `.agents/*.md` guidance documents.
- **Versioning:** Amendments follow semantic versioning (`MAJOR` for backward-incompatible principle removals or redefinitions, `MINOR` for new principles or materially expanded guidance, `PATCH` for clarifications and non-semantic refinements).
- **Compliance:** All pull requests, Spec Kit plans (`Constitution Check`), and AI-assisted implementations MUST verify compliance with these principles and pass `ArchitectureLayerTest`.

**Version**: 1.1.1 | **Ratified**: 2026-11-22 | **Last Amended**: 2026-11-23
