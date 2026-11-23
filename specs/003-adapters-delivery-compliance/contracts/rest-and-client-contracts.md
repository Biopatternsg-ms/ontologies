# REST & Internal Client Contracts

**Feature**: `003-adapters-delivery-compliance`  
**Date**: 2026-11-23

## 1. REST Endpoints (External Wire Contracts Preserved)

- `POST /gene-ontology/build-tree`:
  - **Request Body**: `BuildGeneOntologyTreeRequest` (`cellularComponent`, `molecularFunction`, `biologicalProcess`)
  - **202 Accepted Body**: `MessageResponse("Building Gene Ontology tree")` -> `{"message": "Building Gene Ontology tree"}`
- `POST /mesh-ontology/search-mesh-id`:
  - **404 Not Found Body**: `MessageResponse("MeSH term not found for provided synonyms")` -> `{"message": "MeSH term not found for provided synonyms"}`
- `POST /mesh-ontology/mesh`:
  - **Request Body**: `BuildMeshTreeRequest` (`id`, `name`, `symbol`, `synonyms`)
  - **202 Accepted Body**: `MessageResponse("Building mesh tree")` -> `{"message": "Building mesh tree"}`

## 2. Internal HTTP Client (`BiologicalObjectsHttpClient`)

- `@PATCH @Path("biological-object/update-mesh-id")`
- **Method**: `Response updateMeshId(@RequestBody UpdateMeshIdRequest updateMeshIdRequest);`
