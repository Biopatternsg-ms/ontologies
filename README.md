# ontologies

**`ontologies`** (`com.biopatternsg:ontologies`) is the microservice responsible for **building, storing, searching, and hierarchically classifying biological ontologies (Gene Ontology and MeSH)** within the **BioPatternsG** microservices ecosystem.

Built with **Quarkus 3.24.2 (Java 21)**, **MongoDB**, and **RabbitMQ**, it follows **Hexagonal Architecture (Ports & Adapters)** and **Clean Architecture** principles.

---

## Key Capabilities

- **Gene Ontology (GO) Tree Construction:** Receives GO term IDs asynchronously, filters out already-persisted nodes in MongoDB (`gene_ontology`), fetches missing terms and paths-to-root from the **EBI QuickGO** API, and stores each `GoTerm` with its direct parent relations.
- **Event-Driven MeSH Ontology Tree Construction:** Accepts biological objects via HTTP, deduplicates against `mesh_ontology`, and dispatches unseen items to **RabbitMQ** (`exchange-ncbi` / `mesh-queue`). A background consumer resolves MeSH descriptors via **NCBI E-utilities** (`esearch.fcgi` and `esummary.fcgi`), iteratively persists all ancestor nodes in MongoDB (`mesh_ontology`), and notifies `search-biological-objects` with the resolved `meshId`.
- **MeSH ID Lookup by Synonyms:** Performs case-insensitive exact lookups across `name` and `synonyms` in `mesh_ontology` to resolve a matching `meshId` without external API calls.
- **Hierarchical MeSH Category Classification:** Traverses stored MeSH ancestors via upward Breadth-First Search (stopping at root `1000048`) to classify nodes across `MeshCategory` values (`PROTEIN`, `ENZYME`, `RECEPTOR`, `LIGAND`, `TRANSCRIPTION_FACTOR`, `ADAPTOR_PROTEIN`), caching classification results directly in `mesh_ontology`.

---

## Tech Stack

- **Language & Runtime:** Java 21
- **Framework:** Quarkus `3.24.2` (`quarkus-arc`, `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-rest-jaxb`, `quarkus-rest-client`, `quarkus-rest-client-jackson`, `quarkus-rest-client-jaxb`, `quarkus-hibernate-validator`)
- **Database:** MongoDB (`quarkus-mongodb-panache`)
- **Messaging:** RabbitMQ (`quarkus-messaging-rabbitmq` / SmallRye Reactive Messaging)
- **API Docs:** SmallRye OpenAPI & Swagger UI (`quarkus-smallrye-openapi`, `quarkus-swagger-ui`)
- **Utilities & Build:** Lombok `1.18.34`, License Maven Plugin (`com.mycila`)
- **Quality & Architecture Gates:** ArchUnit (`archunit-junit5`), JUnit 5, REST Assured

---

## Documentation & Governance References

All architectural guidelines, coding standards, and functional specifications for developers and AI agents are maintained in this repository:

| Document | Description |
|---|---|
| [Architecture Overview](.agents/architecture.md) | Hexagonal layer diagram, package structure, MongoDB collections, RabbitMQ topology, external/internal integrations, and Use Case index. |
| [Architectural Rules & Standards](.agents/architecture-rules.md) | Mandatory Hexagonal boundaries, Use Case / Application Service encapsulation, REST/RabbitMQ adapter rules, and verification gates. |
| [Java 21 & Clean Code Rules](.agents/java-rules.md) | Parameter limits, lambda cleanliness, constructor injection, null safety, `@ToString`, CWE-117 log sanitization (`LogSanitizer`), and static mapper conventions. |
| [Use Cases Catalog](.agents/use_cases/README.md) | Complete index and detailed specifications (contracts, execution flows, Mermaid sequence diagrams, and caching/failure scenarios) for every Use Case. |
| [Project Constitution (Spec Kit)](.specify/memory/constitution.md) | Authoritative non-negotiable governance principles and semantic versioning policy for Spec-Driven Development (SDD) workflows. |

---

## Prerequisites

To build and run `ontologies` locally, you need:

1. **JDK 21** (e.g., Eclipse Temurin 21 / OpenJDK 21).
   > **Note:** Ensure `JAVA_HOME` points to JDK 21 (matching `<maven.compiler.release>21</maven.compiler.release>` and Lombok `1.18.34` annotation processing).
2. **Maven 3.9+** (or use the bundled Maven Wrapper `./mvnw`).
3. **MongoDB** (local or containerized instance accessible via connection string, authenticating against the `admin` auth source; stores the `gene_ontology` and `mesh_ontology` collections).
4. **RabbitMQ** (local or containerized broker reachable over AMQP port `5672` for asynchronous MeSH tree construction).
5. **Docker** *(optional)*, for building and running the service container.
6. **Network / Ecosystem Dependencies:**
   - **Internal BioPatternsG Service:** `search-biological-objects` (`biological-objects-api`), required to persist resolved `meshId` values back onto biological objects (`PATCH /biological-object/update-mesh-id`).
   - **External Biological APIs:** Outbound HTTPS access to **EBI QuickGO** (`https://www.ebi.ac.uk/QuickGO/services/ontology/go/terms`) and **NCBI E-utilities** (`https://eutils.ncbi.nlm.nih.gov/entrez/eutils`).

---

## Environment Configuration

Configuration is defined in [`src/main/resources/application.properties`](src/main/resources/application.properties) and driven by the following environment variables:

### HTTP Server, MongoDB & Internal Services

| Variable | Required | Default | Description |
|---|---|---|---|
| `PORT` | No | `8080` | HTTP server port. |
| `DB_URL` | No | `mongodb://localhost:27017` | MongoDB connection string. |
| `DB_USER` | **Yes** | — | MongoDB username (authenticated against `admin`). |
| `DB_PASSWORD` | **Yes** | — | MongoDB password. |
| `DB_NAME` | **Yes** | — | Target MongoDB database name. |
| `BIOLOGICAL_OBJECTS` | No | `http://biological-objects:8080` | Base URL for the `search-biological-objects` microservice (`biological-objects-api`). |

### RabbitMQ Messaging

| Variable | Required | Default | Description |
|---|---|---|---|
| `RABBIT_HOST` | No | `localhost` | RabbitMQ broker hostname. |
| `RABBIT_PORT` | No | `5672` | RabbitMQ AMQP port. |
| `RABBIT_USER` | **Yes** | — | RabbitMQ username. |
| `RABBIT_PASSWORD` | **Yes** | — | RabbitMQ password. |

**Messaging Topology (Pre-configured):**
- **Exchange:** `exchange-ncbi`
- **Queue / Routing Key:** `mesh-queue`
- **Outgoing Channel (`ncbi-out`):** Publishes `BiologicalObject` events (`MeshQueueSenderRabbitAdapter`).
- **Incoming Channel (`ncbi-in`):** Consumes `BiologicalObject` events (`MeshQueueConsumerRabbitImpl`).

### External Biological APIs (Pre-configured Defaults)

- **EBI QuickGO (`ebi-api`):** `https://www.ebi.ac.uk/QuickGO/services/ontology/go/terms` (JSON)
- **NCBI E-utilities (`ncbi-api`):** `https://eutils.ncbi.nlm.nih.gov/entrez/eutils` (XML/JAXB with `DoctypeRemovalFilter`)

---

## Running the Application

### 1. Development Mode (Live Coding)

Export the required MongoDB and RabbitMQ environment variables and launch Quarkus in dev mode:

```bash
export DB_URL="mongodb://localhost:27017"
export DB_USER="your_db_user"
export DB_PASSWORD="your_db_password"
export DB_NAME="biopatternsg"
export RABBIT_HOST="localhost"
export RABBIT_PORT="5672"
export RABBIT_USER="your_rabbit_user"
export RABBIT_PASSWORD="your_rabbit_password"
export BIOLOGICAL_OBJECTS="http://localhost:8081"

./mvnw quarkus:dev
```

Useful local endpoints when running:
- **Swagger UI (OpenAPI):** `http://localhost:8080/q/swagger-ui`
- **OpenAPI Spec:** `http://localhost:8080/q/openapi`
- **Quarkus Dev UI** *(dev mode only)*: `http://localhost:8080/q/dev/`

### 2. Packaging and Running as JVM Application

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

To build an über-jar instead:

```bash
./mvnw clean package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### 3. Building and Running with Docker

First package the Quarkus application so `target/quarkus-app` is generated, then build and run using the root [`Dockerfile`](Dockerfile):

```bash
./mvnw clean package -DskipTests
docker build -t biopatternsg/ontologies:latest .
docker run --rm -p 8080:8080 \
  -e DB_URL="mongodb://host.docker.internal:27017" \
  -e DB_USER="your_db_user" \
  -e DB_PASSWORD="your_db_password" \
  -e DB_NAME="biopatternsg" \
  -e RABBIT_HOST="host.docker.internal" \
  -e RABBIT_PORT="5672" \
  -e RABBIT_USER="your_rabbit_user" \
  -e RABBIT_PASSWORD="your_rabbit_password" \
  -e BIOLOGICAL_OBJECTS="http://biological-objects:8080" \
  biopatternsg/ontologies:latest
```

### 4. Native Executable *(Optional)*

```bash
./mvnw package -Dnative
# Or using a container build without a local GraalVM installation:
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

---

## REST API & Messaging Overview

Full request/response schemas are available via Swagger UI at `/q/swagger-ui`.

| Method | Path | Controller | Description |
|---|---|---|---|
| `POST` | `/gene-ontology/build-tree` | `GeneOntologyController` | Triggers asynchronous Gene Ontology tree construction from GO term IDs (`202 Accepted`). |
| `POST` | `/mesh-ontology/mesh` | `MeshOntologyController` | Validates and enqueues a biological object to RabbitMQ (`mesh-queue`) for MeSH tree construction (`202 Accepted`). |
| `POST` | `/mesh-ontology/search-mesh-id` | `MeshOntologyController` | Searches `mesh_ontology` case-insensitively by synonyms to resolve a `meshId` (`200 OK` / `404 Not Found`). |
| `POST` | `/mesh-ontology/check-type` | `MeshOntologyController` | Evaluates whether a MeSH node or its ancestors match a single `MeshCategory` (`200 OK`). |
| `POST` | `/mesh-ontology/check-all-types` | `MeshOntologyController` | Evaluates and caches all `MeshCategory` classifications for a MeSH node (`200 OK`). |

**Asynchronous Messaging Entry Point:**
- **RabbitMQ Consumer (`ncbi-in` / `mesh-queue`):** `MeshQueueConsumerRabbitImpl` consumes `BiologicalObject` messages and triggers `BuildMeshOntologyTree`.

---

## Testing & Quality Gates

Run the full test suite:

```bash
./mvnw test
```

Run the mandatory **Hexagonal Architecture (ArchUnit)** and **Use Case Documentation Governance** gates:

```bash
./mvnw clean compile
./mvnw test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest
```

Verify Apache 2.0 license headers across source files:

```bash
./mvnw license:check
```

---

## License

Copyright © 2026 biopatternsg (`biopatternsg@gmail.com`).  
Licensed under the **Apache License, Version 2.0**.
