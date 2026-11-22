# Arquitectura del Servicio `ontologies`

> **Documento para Agentes de IA y Desarrolladores**  
> **Servicio:** `ontologies` (`artifactId: ontologies`)  
> **Ecosistema:** BioPatternsG Microservices  
> **Framework:** Quarkus 3.24.2 (Java 21)  
> **Estilo de Arquitectura:** Arquitectura Hexagonal (Ports & Adapters) / Clean Architecture

---

## 1. Propósito del Servicio

`ontologies` es el microservicio encargado de la **construcción, almacenamiento, búsqueda y clasificación jerárquica de ontologías biológicas (Gene Ontology y MeSH)** dentro del ecosistema **BioPatternsG**. Sus responsabilidades principales incluyen:

1. **Construcción del Árbol de Gene Ontology (`GoTerm`):** Recibe identificadores de términos GO (`POST /gene-ontology/build-tree`), filtra los que aún no existen en base de datos, consulta sus metadatos y rutas hacia la raíz en **EBI QuickGO**, y persiste los nodos junto con sus relaciones padre (`parentRelations`) en MongoDB (`gene_ontology`).
2. **Construcción Asíncrona del Árbol de Ontología MeSH (`MeshInfo`):** Recibe objetos biológicos (`POST /mesh-ontology/mesh`), verifica si ya existe su árbol en `mesh_ontology` y los despacha a una cola **RabbitMQ** (`mesh-queue`). Un consumidor asíncrono consulta **NCBI E-utilities** (`esearch.fcgi` y `esummary.fcgi`), resuelve el término MeSH por coincidencia de sinónimos, recorre y guarda iterativamente todos sus nodos ancestros en `mesh_ontology`, y notifica el `meshId` resuelto al microservicio `search-biological-objects`.
3. **Búsqueda de `meshId` por Sinónimos:** Permite resolver un identificador MeSH (`POST /mesh-ontology/search-mesh-id`) mediante búsqueda insensible a mayúsculas sobre `name` y `synonyms` almacenados en `mesh_ontology`.
4. **Clasificación Jerárquica de Categorías MeSH (`MeshCategory`):** Evalúa si un nodo MeSH o alguno de sus ancestros (mediante recorrido BFS hasta la raíz `1000048`) pertenece a una categoría específica (`POST /mesh-ontology/check-type`) o a todas las categorías soportadas (`PROTEIN`, `ENZYME`, `RECEPTOR`, `LIGAND`, `TRANSCRIPTION_FACTOR`, `ADAPTOR_PROTEIN` en `POST /mesh-ontology/check-all-types`), persistiendo los resultados en caché sobre el documento en `mesh_ontology`.

---

## 2. Diagrama Conceptual de Capas (Arquitectura Hexagonal)

```
+---------------------------------------------------------------------------------------+
|                                    INFRASTRUCTURE                                     |
|                                                                                       |
|   [Inbound Adapters]                                                                  |
|   - REST Controllers: GeneOntologyController, MeshOntologyController                  |
|   - RabbitMQ Consumer: MeshQueueConsumerRabbitImpl (@Incoming("ncbi-in"))             |
|                                         |                                             |
|                                         v (invoca a través de Ports IN)               |
|   +-------------------------------------+-----------------------------------------+   |
|   |                                     |                                         |   |
|   |         +---------------------------v---------------------------+             |   |
|   |         |                      APPLICATION                      |             |   |
|   |         |                                                       |             |   |
|   |         |   [Application Services & Use Cases]                  |             |   |
|   |         |   - Services: GeneOntologyTermService,                |             |   |
|   |         |     MeshOntologyService, BiologicalObjectsService     |             |   |
|   |         |   - Use Cases (application.usecase):                  |             |   |
|   |         |     Implementan los puertos de entrada (port.in)      |             |   |
|   |         |                               |                       |             |   |
|   |         |                               v (utiliza)             |             |   |
|   |         |   +---------------------------v-------------------+   |             |   |
|   |         |   |                     DOMAIN                    |   |             |   |
|   |         |   |                                               |   |             |   |
|   |         |   |   - Models: GoTerm, PathsToRoot, PathToRoot,  |   |             |   |
|   |         |   |     GeneOntologyBuildTreeRequest, MeshInfo,   |   |             |   |
|   |         |   |     SummaryMesh, BiologicalObject,            |   |             |   |
|   |         |   |     MeshCategory (enum)                       |   |             |   |
|   |         |   |   - Ports IN (Interfaces de Casos de Uso)     |   |             |   |
|   |         |   |   - Ports OUT (repositories,                  |   |             |   |
|   |         |   |     external_repositories, producers,         |   |             |   |
|   |         |   |     consumers)                                |   |             |   |
|   |         |   +---------------------------^-------------------+   |             |   |
|   |         |                               |                       |             |   |
|   |         +-------------------------------+-----------------------+             |   |
|   |                                         ^                                     |   |
|   |                                         | (implementan Ports OUT)             |   |
|   +-----------------------------------------+-------------------------------------+   |
|                                         ^                                             |
|                                         |                                             |
|   [Outbound Adapters (Persistence, Messaging, Internal & External Services)]          |
|   - MongoDB Panache: GeneOntologyTermRepositoryAdapter, MeshOntologyRepositoryAdapter |
|   - RabbitMQ Producer: MeshQueueSenderRabbitAdapter (@Channel("ncbi-out"))            |
|   - Internal Microservices: BiologicalObjectsAdapter (search-biological-objects)      |
|   - External APIs: GOAdapter (EBI QuickGO), MeshAdapter (NCBI E-utilities MeSH)       |
+---------------------------------------------------------------------------------------+
```

---

## 3. Estructura de Paquetes (`com.biopatternsg.*`)

```text
com.biopatternsg/
├── Main.java                                <-- Punto de entrada de la aplicación Quarkus (@QuarkusMain)
├── domain/                                  <-- Capa de Dominio (Pura, sin dependencias de frameworks)
│   ├── model/                               <-- Entidades y modelos del dominio
│   │   ├── GeneOntologyBuildTreeRequest     <-- Solicitud para construir árbol de Gene Ontology
│   │   ├── GoTerm                           <-- Término de Gene Ontology (Synonym, ParentRelation)
│   │   ├── PathsToRoot / PathToRoot         <-- Rutas jerárquicas hacia la raíz en GO
│   │   └── mesh/                            <-- Modelos y clasificador MeSH (BiologicalObject, MeshInfo, SummaryMesh, MeshCategory, ESearchResult, ESummaryResult)
│   └── port/
│       ├── in/                              <-- Puertos de Entrada (Interfaces de Use Cases)
│       └── out/                             <-- Puertos de Salida
│           ├── consumers/                   <-- Contrato de consumidor de cola (MeshQueueConsumer)
│           ├── producers/                   <-- Puertos de publicación de mensajes (MeshQueueSender)
│           ├── repositories/                <-- Puertos de persistencia MongoDB (GeneOntologyTermRepository, MeshOntologyRepository)
│           └── external_repositories/       <-- Puertos hacia APIs externas y servicios internos (GeneOntologyTermRepoWeb, MeshOntologyRepoWeb, BiologicalObjectsRepository)
│
├── application/                             <-- Capa de Aplicación
│   ├── services/                            <-- Interfaces de servicios de aplicación (GeneOntologyTermService, MeshOntologyService, BiologicalObjectsService)
│   │   └── impl/                            <-- Implementaciones (@ApplicationScoped: GeneOntologyTermServiceImpl, MeshOntologyServiceImpl, BiologicalObjectsServiceImp)
│   └── usecase/                             <-- Casos de uso concretos (*UseCase.java)
│
└── infrastructure/                          <-- Capa de Infraestructura (Quarkus, MongoDB, RabbitMQ, HTTP)
    ├── adapters/
    │   ├── in/
    │   │   ├── consumers/                   <-- Consumidores RabbitMQ (MeshQueueConsumerRabbitImpl)
    │   │   └── restcontrollers/             <-- Controladores REST (*Controller.java: GeneOntologyController, MeshOntologyController)
    │   └── out/
    │       ├── GeneOntologyTermRepositoryAdapter   <-- Adaptador MongoDB para GO
    │       ├── MeshOntologyRepositoryAdapter       <-- Adaptador MongoDB para MeSH
    │       ├── external_repositories/              <-- Adaptadores hacia APIs externas y servicios internos (GOAdapter, MeshAdapter, BiologicalObjectsAdapter)
    │       └── producers/                          <-- Adaptador productor RabbitMQ (MeshQueueSenderRabbitAdapter)
    ├── clients/
    │   ├── external_clients/                <-- Clientes HTTP MicroProfile RestClient (GeneOntologyHttpClient, MeshOntologyHttpClient, DoctypeRemovalFilter)
    │   └── internal_clients/                <-- Clientes HTTP internos (BiologicalObjectsHttpClient)
    ├── dtos/                                <-- DTOs Java records de entrada/salida (CheckMeshType*, CheckAllMeshTypes*, SearchMeshIdRequest, MeshIdResponse, UpdateMeshIdRequest)
    ├── external_services/                   <-- Wrappers y DTOs de servicios externos (QueryGO, QueryMesh, impl/*, dto/go/GoTermResponse)
    ├── internal_services/                   <-- Wrappers de servicios internos (QueryBiologicalObjects, impl/QueryBiologicalObjectsImpl)
    ├── mapper/                              <-- Mappers de conversión entre capas (GoTermMapper)
    └── mongo/                               <-- Colecciones PanacheMongoEntity (GoTermCollection, MeshTermCollection)
```

---

## 4. Reglas Arquitectónicas Estrictas (Enforced by ArchUnit)

Cualquier cambio de código realizado por un agente **DEBE** respetar las reglas validadas en `src/test/java/archtest/ArchitectureLayerTest.java`:

| Regla | Descripción |
|---|---|
| **Aislamiento de Dominio** | `com.biopatternsg.domain..` **NO** puede depender de `application..` ni de `infrastructure..`. |
| **Aislamiento de Aplicación** | `com.biopatternsg.application..` **NO** puede depender de `infrastructure..`. |
| **Encapsulamiento de Aplicación (`layerApplicationTest`)** | `com.biopatternsg.application..` solo puede ser accedida por clases de la misma capa Application (la comunicación desde Infrastructure se realiza exclusivamente mediante interfaces en `domain.port.in`). |
| **Acceso a Casos de Uso (`layerApplicationUsesCasesTest`)** | Las implementaciones en `application.usecase..` no pueden ser accedidas por ninguna capa; se interactúa exclusivamente a través de sus puertos `domain.port.in`. |
| **Nomenclatura UseCase** | Toda clase en `application.usecase..` debe terminar en `UseCase` (ej. `BuildGeneOntologyTreeUseCase`, `CheckMeshTermTypeUseCase`). |
| **Nomenclatura Controller** | Toda clase en `infrastructure.adapters.in.restcontrollers..` debe terminar en `Controller` (ej. `GeneOntologyController`, `MeshOntologyController`). |
| **Nomenclatura Adapter** | Toda clase en `infrastructure.adapters.out..` debe terminar en `Adapter` (ej. `GeneOntologyTermRepositoryAdapter`, `MeshOntologyRepositoryAdapter`, `GOAdapter`, `MeshAdapter`, `BiologicalObjectsAdapter`, `MeshQueueSenderRabbitAdapter`). |

---

## 5. Casos de Uso Principales

Ubicación: `com.biopatternsg.application.usecase.*` (Catálogo e índice general disponible en [`.agents/use_cases/README.md`](use_cases/README.md)):

| Caso de Uso | Puerto In | Canal de Entrada | Responsabilidad | Documento |
|---|---|---|---|---|
| `BuildGeneOntologyTreeUseCase` | `BuildGeneOntologyTree` | REST (`POST /gene-ontology/build-tree`, asíncrono) | Filtra los GO IDs no almacenados en MongoDB, consulta sus metadatos y rutas hacia la raíz en EBI QuickGO y guarda cada `GoTerm` con sus `parentRelations`. | [`BuildGeneOntologyTreeUseCase.md`](use_cases/BuildGeneOntologyTreeUseCase.md) |
| `SendBiologicalObjectToQueueUseCase` | `SendBiologicalObjectToQueue` | REST (`POST /mesh-ontology/mesh`, asíncrono) | Normaliza `name`/`symbol` del `BiologicalObject`, valida que no exista ya en `mesh_ontology` (por nombre o sinónimos) y lo publica en la cola RabbitMQ (`ncbi-out`). | [`SendBiologicalObjectToQueueUseCase.md`](use_cases/SendBiologicalObjectToQueueUseCase.md) |
| `BuildMeshOntologyTreeUseCase` | `BuildMeshOntologyTree` | RabbitMQ (`ncbi-in` / `mesh-queue`) | Busca términos MeSH en NCBI E-utilities, identifica la primera coincidencia por sinónimos, persiste el nodo y recorre iterativamente sus ancestros en `mesh_ontology`, y actualiza `meshId` en `search-biological-objects`. | [`BuildMeshOntologyTreeUseCase.md`](use_cases/BuildMeshOntologyTreeUseCase.md) |
| `SearchMeshIdBySynonymsUseCase` | `SearchMeshIdBySynonyms` | REST (`POST /mesh-ontology/search-mesh-id`) | Limpia y normaliza la lista de sinónimos y busca de forma insensible a mayúsculas el primer `meshId` coincidente en `mesh_ontology`. | [`SearchMeshIdBySynonymsUseCase.md`](use_cases/SearchMeshIdBySynonymsUseCase.md) |
| `CheckMeshTermTypeUseCase` | `CheckMeshTermType` | REST (`POST /mesh-ontology/check-type` y `POST /mesh-ontology/check-all-types`) | Recorre por BFS la jerarquía de ancestros en `mesh_ontology` (deteniéndose en `1000048`) para evaluar pertenencia a una o todas las `MeshCategory`, cacheando los resultados en el documento. | [`CheckMeshTermTypeUseCase.md`](use_cases/CheckMeshTermTypeUseCase.md) |

---

## 6. Persistencia y Mensajería

### 6.1. Persistencia (MongoDB Panache)
- **Motor:** MongoDB.
- **Acceso a Datos:** Quarkus MongoDB con Panache (`PanacheMongoRepository` / `PanacheMongoEntity`).
- **Colecciones Principales (`infrastructure/mongo/`):**
    - `GoTermCollection` (`gene_ontology`): Almacena términos de Gene Ontology (`termId`, `name`, `synonyms`, `parentRelations`).
    - `MeshTermCollection` (`mesh_ontology`): Almacena nodos de la ontología MeSH (`meshId`, `name`, `synonyms`, `parents`, `categories`).

### 6.2. Mensajería Asíncrona (RabbitMQ)
- **Conector:** SmallRye Reactive Messaging RabbitMQ (`quarkus-messaging-rabbitmq`).
- **Topología (`src/main/resources/application.properties`):**
    - **Exchange:** `exchange-ncbi`
    - **Cola / Routing Key:** `mesh-queue`
    - **Productor (`ncbi-out`):** `MeshQueueSenderRabbitAdapter` despacha objetos `BiologicalObject`.
    - **Consumidor (`ncbi-in`):** `MeshQueueConsumerRabbitImpl` (`@Blocking`) consume mensajes de `mesh-queue` y dispara `BuildMeshOntologyTree`.

---

## 7. Integraciones Internas y Externas (MicroProfile REST Clients)

Configuradas en `src/main/resources/application.properties`:

### Servicios Internos del Ecosistema
- **`search-biological-objects` (`biological-objects-api`):** Actualiza el `meshId` resuelto en un objeto biológico mediante `PATCH /biological-object/update-mesh-id` (`BiologicalObjectsHttpClient` -> `QueryBiologicalObjects` -> `BiologicalObjectsAdapter`).

### APIs Biológicas Externas
- **EBI QuickGO (`ebi-api`, `https://www.ebi.ac.uk/QuickGO/services/ontology/go/terms`):** Consulta detalles de términos GO y rutas hacia las raíces (`GO:0008150;GO:0003674;GO:0005575`) en JSON (`GeneOntologyHttpClient` -> `QueryGO` -> `GOAdapter`).
- **NCBI E-utilities MeSH (`ncbi-api`, `https://eutils.ncbi.nlm.nih.gov/entrez/eutils`):** Consulta `esearch.fcgi` y `esummary.fcgi` para la base `mesh` en XML/JAXB usando `DoctypeRemovalFilter` para sanitizar declaraciones `DOCTYPE` (`MeshOntologyHttpClient` -> `QueryMesh` -> `MeshAdapter`).

---

## 8. Guía de Intervención y Estándares de Código

Para el conjunto completo de reglas mandatorias de arquitectura y estándares de lenguaje Java, consulte los documentos:
- 👉 [**`.agents/architecture-rules.md`**](architecture-rules.md) — Reglas de Arquitectura Hexagonal, límites de capas, casos de uso, servicios y adaptadores REST/RabbitMQ.
- 👉 [**`.agents/java-rules.md`**](java-rules.md) — Reglas de Java 21, Clean Code, lambdas, límite de parámetros y mappers estáticos.

### Resumen Rápido para Nuevas Funcionalidades:
1. **Nuevo Caso de Uso:**
    1. Crear la interfaz del puerto de entrada en `domain/port/in/MiOperacion.java`.
    2. Implementar la clase en `application/usecase/MiOperacionUseCase.java` anotada con `@ApplicationScoped`.
    3. Documentar el caso de uso en `.agents/use_cases/MiOperacionUseCase.md` (en inglés) y actualizar `.agents/use_cases/README.md` y la tabla de la sección 5 de `.agents/architecture.md`.
    4. Exponerlo en el controlador REST (`infrastructure/adapters/in/restcontrollers/`) o consumidor (`infrastructure/adapters/in/consumers/`) inyectando únicamente la interfaz `MiOperacion`.
2. **Nueva Integración / Repositorio / Productor:**
    1. Crear la interfaz del puerto de salida en `domain/port/out/repositories/`, `domain/port/out/external_repositories/` o `domain/port/out/producers/`.
    2. Implementar el adaptador correspondiente bajo `infrastructure/adapters/out/` terminando en `Adapter`.
3. **Verificación Obligatoria:**
    - Ejecutar `./mvnw clean compile` para asegurar compilación exitosa y actualizar `target/classes`.
    - Ejecutar `./mvnw test -Dtest=ArchitectureLayerTest` para comprobar que las reglas de arquitectura no se hayan violado.
