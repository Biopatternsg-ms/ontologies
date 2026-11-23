# Data Model: Inbound and Outbound Adapters Delivery Layer Compliance

**Feature**: `003-adapters-delivery-compliance`  
**Date**: 2026-11-23

## 1. Infrastructure Request & Response DTO Records (`com.biopatternsg.infrastructure.dtos`)

### `BuildGeneOntologyTreeRequest`
- **Type**: `public record BuildGeneOntologyTreeRequest(List<String> cellularComponent, List<String> molecularFunction, List<String> biologicalProcess)`
- **Mapping**: `OntologyRequestMapper.requestToModel(BuildGeneOntologyTreeRequest)` -> `GeneOntologyBuildTreeRequest` (normalizes `null` lists to `List.of()` so `getGoTermIds()` never NPEs).

### `BuildMeshTreeRequest`
- **Type**: `public record BuildMeshTreeRequest(String id, String name, String symbol, List<String> synonyms)`
- **Mapping**: `OntologyRequestMapper.requestToModel(BuildMeshTreeRequest)` -> `BiologicalObject`.

### `MessageResponse`
- **Type**: `public record MessageResponse(String message)`
- **Serialized JSON**: `{"message": "<text>"}`
