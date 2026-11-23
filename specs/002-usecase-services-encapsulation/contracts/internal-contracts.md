# Internal Service & Repository Contracts

**Feature**: `002-usecase-services-encapsulation`  
**Date**: 2026-11-23

## 1. `BiologicalObjectsServiceImpl` (`com.biopatternsg.application.services.impl`)
- **Renamed from**: `BiologicalObjectsServiceImp`
- **Implements**: `com.biopatternsg.application.services.BiologicalObjectsService`
- **Method**: `public void updateMeshId(String biologicalObjectId, String meshId)`

## 2. `MeshOntologyRepository` (`com.biopatternsg.domain.port.out.repositories`)
- `updateCategories(String meshId, Map<String, Boolean> categories)` — abstract (non-default)
- `updateCategory(String meshId, String category, boolean isType)` — abstract (non-default)

## 3. Use Case Public Contracts (Unchanged)
- `BuildMeshOntologyTree.execute(BiologicalObject biologicalObject)` — non-mutating on `biologicalObject`; checks local existence before NCBI calls.
- `SendBiologicalObjectToQueue.execute(BiologicalObject biologicalObject)` — non-mutating on `biologicalObject`.
- `CheckMeshTermType.execute(String meshId, MeshCategory targetType)` / `executeAll(String meshId)` — identical external behavior with deduplicated internal BFS traversal.
- `BuildGeneOntologyTree.execute(GeneOntologyBuildTreeRequest geneOntology)` — identical external behavior with `Set<String>` lookup and clean helper method.
