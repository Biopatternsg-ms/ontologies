# Architectural Rules and Design Standards

> **Service:** `ontologies`  
> **Package Base:** `com.biopatternsg.*`  
> **Framework:** Quarkus 3.24.2 (Java 21) | MongoDB Panache | SmallRye Reactive Messaging (RabbitMQ)  
> **Architecture:** Hexagonal (Ports & Adapters) / Clean Architecture

This document establishes the mandatory **architectural rules, layer boundaries, and component responsibilities** that developers and AI agents must follow when creating or modifying features in the `ontologies` service.

*(For Java language, clean code, lambdas, and static mapper coding rules, see [`java-rules.md`](java-rules.md)).*

---

## 1. Hexagonal Layer Boundaries & Isolation

1. **Domain Layer (`com.biopatternsg.domain.*`):**
    - Pure business core (`model`, `model.mesh`, `port.in`, `port.out`).
    - **MUST NOT** depend on `application..`, `infrastructure..`, or external delivery/persistence frameworks (e.g., `jakarta.ws.rs.*`, MongoDB Panache, RabbitMQ/Vert.x).
2. **Application Layer (`com.biopatternsg.application.*`):**
    - Contains Use Cases (`application.usecase.*`) and Application Services (`application.services.*`, `application.services.impl.*`).
    - **MUST** depend only on `domain..` and **MUST NOT** depend on `infrastructure..`.
    - Can only be accessed from outer layers through Inbound Ports (`domain.port.in.*`).
3. **Infrastructure Layer (`com.biopatternsg.infrastructure.*`):**
    - Contains inbound delivery adapters (`adapters.in.restcontrollers.*`, `adapters.in.consumers.*`), outbound persistence, messaging, and external/internal adapters (`adapters.out.*`, `adapters.out.external_repositories.*`, `adapters.out.producers.*`), DTOs (`dtos.*`), MicroProfile HTTP clients (`clients.external_clients.*`, `clients.internal_clients.*`), service wrappers (`external_services.*`, `internal_services.*`), mappers (`mapper.*`), and MongoDB collections (`mongo.*`).

---

## 2. Use Cases (`com.biopatternsg.application.usecase`)

### 2.1. Single Entry-Point Responsibility
- Each Use Case represents a single business interaction and implements its dedicated Inbound Port interface defined under `com.biopatternsg.domain.port.in.*` (e.g., `BuildGeneOntologyTree`, `BuildMeshOntologyTree`, `SendBiologicalObjectToQueue`, `SearchMeshIdBySynonyms`, `CheckMeshTermType`).
- Use Cases must implement only the contract methods defined by their inbound port interface (typically `execute(...)` or cohesive variants like `executeAll(...)`).
- Concrete Use Case classes (`*UseCase.java`) must never be directly injected or instantiated across layers; callers (REST controllers and message consumers) must always depend on the Inbound Port interface.

### 2.2. Use Cases as Lightweight Orchestrators
Implementations (`*UseCase.java`) must remain lean and act purely as orchestrators:
- Validate input preconditions and business constraints (e.g., null/blank checks, synonym normalization, idempotency checks against already-persisted ontology terms).
- Coordinate execution by invoking outbound ports (`domain.port.out.repositories.*`, `domain.port.out.producers.*`) or application services (`application.services.*`).
- Delegate ontology retrieval, external API coordination, and cross-service notifications to **Application Services** (`GeneOntologyTermService`, `MeshOntologyService`, `BiologicalObjectsService`).

### 2.3. Mandatory Use Case Documentation (`.agents/use_cases/`)
- **Mandatory English Language:** All Use Case documentation files under `.agents/use_cases/` (including `README.md` and every `<UseCaseName>.md` file) **must be written strictly in English**.
- **New Use Cases:** Whenever a new Use Case is created, a corresponding markdown document must be created in English under `.agents/use_cases/<UseCaseName>.md` describing:
    1. Business problem and purpose.
    2. Input and output contracts (Inbound port, Outbound ports, Application Services).
    3. Step-by-step execution flow.
    4. Sequence diagram using Mermaid format (`sequenceDiagram`).
    5. Edge cases, caching/idempotency behavior, and validation failure scenarios.
- **Modifications:** Whenever an existing Use Case signature, flow, or rule is modified, its documentation in `.agents/use_cases/<UseCaseName>.md` must be updated synchronously in English.
- **Mandatory Index References (`README.md` & `architecture.md`):** Whenever a new Use Case documentation file is added (or an existing one is renamed/repurposed), its reference and link **must** be added/updated in both:
    1. `.agents/use_cases/README.md` (the Use Cases catalog index).
    2. Section 5 of `.agents/architecture.md`.

---

## 3. Application Services (`com.biopatternsg.application.services`)

### 3.1. Encapsulating Complex Business Logic and Strategies
- Multi-step ontology lookups, hierarchy traversal helpers, external query coordination, and cross-service update orchestration belong in **Application Services**.
- Examples in `ontologies`:
    - `GeneOntologyTermService`: Coordinates Gene Ontology term queries against local MongoDB and EBI QuickGO API (`getByIdsInDB`, `getByIdsInWeb`, `getPathsToRoot`).
    - `MeshOntologyService`: Coordinates MeSH term ID resolution and summary/synonym/parent extraction from NCBI E-utilities (`getMeshTermIds`, `getSummarySynonyms`).
    - `BiologicalObjectsService`: Coordinates `meshId` updates back to the `search-biological-objects` microservice (`updateMeshId`).

### 3.2. Strict Encapsulation and Interface-Implementation Separation
For any new or refactored Application Service:
1. **Interface Contract:** The public contract must be defined as an interface under `com.biopatternsg.application.services.*`.
2. **Hidden Implementation:** The implementation class must reside in `com.biopatternsg.application.services.impl.*` annotated with `@ApplicationScoped`.
3. **Encapsulation:** Concrete implementation classes must not expose internal helper methods publicly. Only the interface contract methods should be visible outside the package.
4. **CDI Injection:** Always inject the **interface** into Use Cases or other Application Services, never the concrete implementation class.

### 3.3. Service Injection and Dependencies
- Application Services may coordinate multiple Outbound Ports (`domain.port.out.repositories.*`, `domain.port.out.external_repositories.*`, and `domain.port.out.producers.*`).
- **Prohibition:** Application Services must **never** be injected directly into REST Controllers or RabbitMQ Consumers; all external traffic must enter via Inbound Ports (`domain.port.in.*`) implemented by Use Cases.

---

## 4. Inbound Adapters (REST Controllers & Messaging Consumers)

### 4.1. Zero Business Logic in REST Controllers (`infrastructure.adapters.in.restcontrollers.*`)
- Controllers (`GeneOntologyController`, `MeshOntologyController`) are strictly HTTP delivery adapters:
    1. Receive and validate HTTP requests (`@Valid`) using DTOs in `com.biopatternsg.infrastructure.dtos.*` (or domain request payloads where currently established).
    2. Convert request DTOs to Domain Models when applicable.
    3. Invoke the Use Case through its Inbound Port interface (`domain.port.in.*`) — either synchronously (`200 OK` / `404 Not Found`) or via managed `Executor` + `CompletableFuture.runAsync(...)` for asynchronous `202 Accepted` endpoints (`/gene-ontology/build-tree`, `/mesh-ontology/mesh`).
    4. Convert the Domain Model result to a Response DTO (e.g., `CheckMeshTypeResponse`, `CheckAllMeshTypesResponse`, `MeshIdResponse`) and return it.
- **No** business branching, ontology traversal, or direct repository queries are allowed inside controller methods.

### 4.2. Messaging Consumers (`infrastructure.adapters.in.consumers.*`)
- RabbitMQ consumers (e.g., `MeshQueueConsumerRabbitImpl` listening on `@Incoming("ncbi-in")` with `@Blocking`) are inbound delivery adapters:
    1. Deserialize incoming queue messages into the expected Domain Model (`BiologicalObject`).
    2. Delegate processing exclusively to the corresponding Inbound Port (`BuildMeshOntologyTree`).
    3. Handle transport/deserialization errors safely with structured logging without embedding domain logic.

### 4.3. Validation and Business Exceptions in Domain/Application Layers
- Structural request validation uses Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@NotEmpty`) on request DTOs.
- Existence checks, idempotency rules, and ontology classification logic belong in Use Cases or Application Services — never in controllers or consumers.

---

## 5. Persistence, Messaging & Outbound Adapters (`com.biopatternsg.infrastructure.adapters.out.*`)

### 5.1. Repository Adapters (`infrastructure.adapters.out.*`)
- Implement repository interfaces defined in `com.biopatternsg.domain.port.out.repositories.*` (`GeneOntologyTermRepository`, `MeshOntologyRepository`) using `PanacheMongoRepository<*>`.
- Separate concerns: MongoDB collections (`infrastructure.mongo.GoTermCollection` -> `gene_ontology`, `infrastructure.mongo.MeshTermCollection` -> `mesh_ontology`) must be converted to Domain Models (`domain.model.*`) via mappers (`infrastructure.mapper.*`) before returning to the application layer.

### 5.2. Messaging Producer Adapters (`infrastructure.adapters.out.producers.*`)
- Implement producer ports defined in `com.biopatternsg.domain.port.out.producers.*` (`MeshQueueSender`).
- `MeshQueueSenderRabbitAdapter` publishes `BiologicalObject` events to RabbitMQ through SmallRye Reactive Messaging (`@Channel("ncbi-out")`, exchange `exchange-ncbi`, routing key `mesh-queue`).

### 5.3. External & Internal Service Adapters (`infrastructure.adapters.out.external_repositories.*`)
- External ontology APIs and internal microservices are consumed via MicroProfile REST Clients (`infrastructure.clients.*`) and service wrappers (`infrastructure.external_services.*`, `infrastructure.internal_services.*`), and exposed to the domain/application layers exclusively through `*Adapter` classes implementing `domain.port.out.external_repositories.*`:
    - **EBI QuickGO API (`ebi-api`):** `GeneOntologyHttpClient` -> `QueryGO` -> `GOAdapter` (implements `GeneOntologyTermRepoWeb`).
    - **NCBI E-utilities MeSH XML API (`ncbi-api`):** `MeshOntologyHttpClient` (with `DoctypeRemovalFilter` + JAXB) -> `QueryMesh` -> `MeshAdapter` (implements `MeshOntologyRepoWeb`).
    - **`search-biological-objects` (`biological-objects-api`):** `BiologicalObjectsHttpClient` -> `QueryBiologicalObjects` -> `BiologicalObjectsAdapter` (implements `BiologicalObjectsRepository`).

---

## 6. Architectural Naming & Verification Gates

### 6.1. Mandatory Naming Conventions (Enforced by ArchUnit)
- Classes in `application.usecase..` must end with `UseCase` (e.g., `BuildGeneOntologyTreeUseCase`, `CheckMeshTermTypeUseCase`).
- Classes in `infrastructure.adapters.in.restcontrollers..` must end with `Controller` (e.g., `GeneOntologyController`, `MeshOntologyController`).
- Classes in `infrastructure.adapters.out..` must end with `Adapter` (e.g., `GeneOntologyTermRepositoryAdapter`, `MeshOntologyRepositoryAdapter`, `GOAdapter`, `MeshAdapter`, `BiologicalObjectsAdapter`, `MeshQueueSenderRabbitAdapter`).

### 6.2. Mandatory Verification Commands
Before completing any task, verify compilation and architectural compliance:
```bash
# 1. Compile classes (required before ArchUnit imports target/classes)
./mvnw clean compile

# 2. Run Hexagonal Architecture and Documentation Governance validation tests
./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest
```
