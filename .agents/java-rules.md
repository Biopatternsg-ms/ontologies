# Java Language and Clean Code Rules

> **Service:** `ontologies`  
> **Language:** Java 21  
> **Framework:** Quarkus 3.24.2 | Lombok

This document establishes the mandatory **Java coding standards, method design, lambda practices, and static mapper conventions** that developers and AI agents must follow when writing or refactoring Java code in `ontologies`.

*(For Hexagonal Architecture boundaries, layer responsibilities, and verification gates, see [`architecture-rules.md`](architecture-rules.md)).*

---

## 1. Method Signatures and Parameter Encapsulation

### 1.1. Maximum 3 Parameters Rule
- No method in a Use Case, Application Service, or Adapter should accept more than **3 individual parameters**.
- When an operation requires more than 3 inputs, encapsulate them into a Java `record`, Domain Model, or Command/Query object (e.g., `GeneOntologyBuildTreeRequest`, `BiologicalObject`, `MeshInfo`, `CheckMeshTypeRequest`, `SearchMeshIdRequest`).

### 1.2. Single-Responsibility Private Methods
- Break down complex multi-step methods into small, single-responsibility private methods with names that express clear intent (e.g., `getNewGOTermIds(...)`, `getParentRelations(...)`, `findFirstMatchTerm(...)`, `normalizeSynonyms(...)`).
- Public methods should read top-to-bottom as a clean sequence of high-level steps without deep nesting (keeping Sonar Cognitive Complexity low).

---

## 2. Clean Code in Streams and Lambda Expressions

### 2.1. Zero Business Logic & Zero Multi-line Builders Inside Lambdas
- Never embed multi-line business calculations, conditional `if/else` branching, try/catch blocks, or multi-line object builders inside stream/optional lambda expressions (`.map(...)`, `.filter(...)`, `.forEach(...)`, `.orElseGet(...)`).
- Lambdas must be single-expression calls or method references (`this::helperMethod`, `GoTermMapper::toModel`, `MeshTermMapper::collectionToModel`) that delegate to descriptive private methods or static mappers.

```java
// ❌ INCORRECT: Multi-line object construction or logic inside lambda
List<GoTerm> terms = collections.stream()
        .map(entity -> {
            GoTerm term = new GoTerm();
            term.setTermId(entity.getTermId());
            term.setName(entity.getName());
            return term;
        })
        .toList();

// ✅ CORRECT: Lambda delegates via method reference to a helper or static mapper
List<GoTerm> terms = collections.stream()
        .map(GoTermMapper::toModel)
        .toList();
```

### 2.2. Modern Java 21 Idioms
- Prefer immutable stream terminal collection `.toList()` over `Collectors.toList()` when mutability is not required.
- Use Java `record` types for immutable DTOs (`com.biopatternsg.infrastructure.dtos.*`, e.g., `CheckMeshTypeRequest`, `CheckMeshTypeResponse`, `CheckAllMeshTypesRequest`, `CheckAllMeshTypesResponse`, `SearchMeshIdRequest`, `MeshIdResponse`, `UpdateMeshIdRequest`) and value objects.
- Use pattern matching and enhanced `switch` expressions (`switch (category) { case PROTEIN -> ... }`) for exhaustive enum handling (e.g., `MeshCategory`) without fall-through bugs.

---

## 3. Dependency Injection and Class Design

### 3.1. Constructor Injection over Field Injection
- Prefer `private final` fields with constructor injection (`@RequiredArgsConstructor` from Lombok or an explicit constructor with `@Inject` / `@RestClient`) over mutable `@Inject` field injection.
- Never leave unused injected fields in any class.

### 3.2. Immutability and Null Safety
- Use `Objects.equals(a, b)` for null-safe equality checks.
- Guard collection and map parameters and return empty collections (`Collections.emptyList()`, `Collections.emptyMap()`, or `List.of()`) rather than `null` for collection return types.
- Avoid dead/no-op statements (such as standalone `Response.ok().build();` calls whose return value is ignored) and ensure JAX-RS `Response` objects returned by REST clients are properly closed (e.g., with `try-with-resources`).

### 3.3. Explicit `toString()` on Domain Models and DTOs (`@ToString`)
- Every class in `domain.model.*` (including `domain.model.mesh.*`) and `infrastructure.dtos.*` (especially when referenced inside Java `record` types or Lombok `@Builder` classes such as `MeshInfo` or `SummaryMesh`) **must** define an explicit `toString()` implementation—typically via Lombok's `@ToString` / `@Data` annotation or a Java `record`—instead of relying on `Object.toString()`.
- This prevents `Use of default toString()` static analysis findings (e.g., GitHub Code Quality / CodeQL) and ensures meaningful log/debug representations.
- Exclude sensitive fields (e.g., passwords, tokens, secrets) with `@ToString.Exclude` whenever applicable.

### 3.4. Log Injection Prevention (CWE-117 / CodeQL)
- **Never** log raw user-provided, RabbitMQ message, or external API values directly via SLF4J (`log.info`, `log.warn`, `log.error`, `log.debug`). Unsanitized line breaks (`\n`, `\r`) allow log forging and trigger GitHub CodeQL `java/log-injection` alerts.
- Always wrap untrusted/external values with `com.biopatternsg.shared.LogSanitizer.sanitize(value)`, which safely converts `null` and replaces `\n` and `\r` with `_`.

```java
// ❌ INCORRECT: Raw user-provided value logged directly (CodeQL java/log-injection)
log.warn("Skipping queue dispatch for biologicalObject id={}", biologicalObject.getId());

// ✅ CORRECT: Untrusted value sanitized before logging
log.warn("Skipping queue dispatch for biologicalObject id={}",
        LogSanitizer.sanitize(biologicalObject.getId()));
```

---

## 4. Static Mappers (`com.biopatternsg.infrastructure.mapper`)

### 4.1. Stateless Utility Classes and Pure Functions
For all new or refactored mapper classes under `com.biopatternsg.infrastructure.mapper.*` (e.g., `GoTermMapper`, `MeshTermMapper`):
- **Class Definition:** Every mapper class must be declared `public final class` with a `private` constructor to prevent instantiation.
- **Pure Functions:**
    - All mapping methods must be `public static`, deterministic, and free of side effects.
    - Zero mutable state or instance fields.
    - No CDI annotations (`@ApplicationScoped`, `@Singleton`, `@RequestScoped`).
    - No runtime reflection or bytecode-generation libraries.
- **Graceful Null Safety:** Every mapping method must safely handle `null` inputs by returning `null` or an empty collection (`List.of()`) without throwing `NullPointerException`.

### 4.2. Standardized Method Naming Conventions
All static mappers should follow clear, consistent naming conventions:

1. **Request DTO → Domain Model:**
    - `requestToModel(XRequest request)`
2. **Database Collection / Entity → Domain Model:**
    - `toGoTerm(GoTermCollection collection)` / `collectionToModel(MeshTermCollection collection)`
    - `toGoTerms(List<GoTermCollection> list)` / `collectionToModelList(List<MeshTermCollection> list)`
3. **Domain Model → Database Collection / Entity:**
    - `toGoTermCollection(GoTerm model)` / `modelToCollection(MeshInfo model)`
    - `modelToCollectionList(List<MeshInfo> list)`
4. **Domain Model → Delivery / Response DTO:**
    - `fromDomain(XModel model)` / `modelToResponse(XModel model)`
    - `modelToResponseList(List<XModel> list)`
5. **External Client / Provider DTO → Domain Model:**
    - `<provider>ToModel(ProviderResponse response)` (e.g., `quickGoToModel(GoTermResponse response)`, `ncbiToModel(ESummaryResult result)`)
    - `<provider>ToModelList(List<ProviderResponse> list)`
