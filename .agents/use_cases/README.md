# Use Cases Catalog — `ontologies`

This directory contains the functional and technical documentation for every Use Case implemented in the `ontologies` microservice (`com.biopatternsg.application.usecase.*`).

> **Language Policy:** All documentation under `.agents/use_cases/` MUST be written in English.

## Index

| Use Case | Inbound Port | Entry Channel | Primary Responsibility | Documentation |
|---|---|---|---|---|
| `BuildGeneOntologyTreeUseCase` | `BuildGeneOntologyTree` | REST (`POST /gene-ontology/build-tree`) | Filters unpersisted GO term IDs, retrieves metadata and paths-to-root from EBI QuickGO, and saves GO terms with parent relations in MongoDB. | [`BuildGeneOntologyTreeUseCase.md`](BuildGeneOntologyTreeUseCase.md) |
| `SendBiologicalObjectToQueueUseCase` | `SendBiologicalObjectToQueue` | REST (`POST /mesh-ontology/mesh`) | Normalizes biological object metadata, skips already-indexed MeSH trees, and publishes new biological objects to RabbitMQ (`ncbi-out`). | [`SendBiologicalObjectToQueueUseCase.md`](SendBiologicalObjectToQueueUseCase.md) |
| `BuildMeshOntologyTreeUseCase` | `BuildMeshOntologyTree` | RabbitMQ (`ncbi-in` / `mesh-queue`) | Resolves MeSH terms via NCBI E-utilities, persists the matched node and all ancestor nodes in `mesh_ontology`, and updates `meshId` in `search-biological-objects`. | [`BuildMeshOntologyTreeUseCase.md`](BuildMeshOntologyTreeUseCase.md) |
| `SearchMeshIdBySynonymsUseCase` | `SearchMeshIdBySynonyms` | REST (`POST /mesh-ontology/search-mesh-id`) | Normalizes input synonyms and performs a case-insensitive lookup in `mesh_ontology` to resolve a matching `meshId`. | [`SearchMeshIdBySynonymsUseCase.md`](SearchMeshIdBySynonymsUseCase.md) |
| `CheckMeshTermTypeUseCase` | `CheckMeshTermType` | REST (`POST /mesh-ontology/check-type`, `POST /mesh-ontology/check-all-types`) | Evaluates single or all `MeshCategory` classifications via BFS over MeSH ancestors and caches results in `mesh_ontology`. | [`CheckMeshTermTypeUseCase.md`](CheckMeshTermTypeUseCase.md) |
