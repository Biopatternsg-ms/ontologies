# Internal Ports & Adapter Contracts

**Feature**: `001-domain-layer-purity`  
**Date**: 2026-11-23

> **Note**: External HTTP/REST, NCBI XML, QuickGO JSON, and RabbitMQ wire contracts remain 100% unchanged. This document records the internal hexagonal port and adapter signature changes.

## 1. Biological Objects Update Port & Adapter

### `BiologicalObjectsRepository` (`com.biopatternsg.domain.port.out.external_repositories`)
- **Before**: `Response updateMeshId(String biologicalObjectId, String meshId);`
- **After**: `void updateMeshId(String biologicalObjectId, String meshId);`

### `BiologicalObjectsAdapter` (`com.biopatternsg.infrastructure.adapters.out.external_repositories`)
- **Contract**:
  - Implements `BiologicalObjectsRepository.updateMeshId(String biologicalObjectId, String meshId)`.
  - Calls `queryBiologicalObjects.updateMeshId(biologicalObjectId, meshId)` inside `try (Response response = ...)`.
  - Logs status (`meshId`, `biologicalObjectId`, `response.getStatus()`) when `response != null`.

### `BiologicalObjectsServiceImp` (`com.biopatternsg.application.services.impl`)
- **Contract**:
  - Delegates `biologicalObjectsRepository.updateMeshId(biologicalObjectId, meshId)` without importing or inspecting `jakarta.ws.rs.core.Response`.

## 2. Gene Ontology Paths-to-Root Client, Service & Adapter

### `GeneOntologyHttpClient` (`com.biopatternsg.infrastructure.clients.external_clients`)
- **After**: `PathsToRootResponse getPathsToRoot(@PathParam("goId") String goId);`

### `QueryGO` / `QueryGoImpl` (`com.biopatternsg.infrastructure.external_services`)
- **After**: `PathsToRootResponse getPathsToRoot(String goId);`

### `GOAdapter` (`com.biopatternsg.infrastructure.adapters.out.external_repositories`)
- **Contract**:
  - Implements `GeneOntologyTermRepoWeb.getPathsToRoot(String goId)` returning `PathsToRoot`.
  - Calls `queryGO.getPathsToRoot(goId)` and maps the `PathsToRootResponse` to `PathsToRoot` via `goTermMapper.toPathsToRoot(response)`.

## 3. Inbound MeSH Queue Consumer

### `MeshQueueConsumerRabbitImpl` (`com.biopatternsg.infrastructure.adapters.in.consumers`)
- **Contract**:
  - Standalone `@ApplicationScoped` bean (no `implements MeshQueueConsumer`).
  - Retains `@Incoming("ncbi-in")` and `@Blocking` on `public void consumer(JsonObject jsonMsg)`.
  - Deserializes `BiologicalObject` and invokes `buildMeshOntologyTree.execute(biologicalObject)`.
