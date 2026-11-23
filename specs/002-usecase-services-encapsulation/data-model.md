# Data Model: Use Cases Lightweight Orchestration and Application Services Encapsulation

**Feature**: `002-usecase-services-encapsulation`  
**Date**: 2026-11-23

> No domain model fields or MongoDB schemas (`gene_ontology`, `mesh_ontology`) are modified. This document captures the helper and repository contract structures introduced or tightened.

## 1. Effective Name Resolution (`BiologicalObjectNameResolver`)

- **Package**: `com.biopatternsg.application.services`
- **Method**: `public static Optional<String> resolveEffectiveName(BiologicalObject biologicalObject)`
- **Rules**:
  - If `biologicalObject == null` -> `Optional.empty()`.
  - If `biologicalObject.getName()` is non-null and non-blank -> `Optional.of(biologicalObject.getName().trim())`.
  - Else if `biologicalObject.getSymbol()` is non-null and non-blank -> `Optional.of(biologicalObject.getSymbol().trim())`.
  - Otherwise -> `Optional.empty()`.
  - **Non-Mutating Guarantee**: Never calls `biologicalObject.setName(...)`.

## 2. `MeshOntologyRepository` Port (`com.biopatternsg.domain.port.out.repositories`)

- **Abstract Methods (no `default` no-op bodies)**:
  - `void updateCategories(String meshId, Map<String, Boolean> categories);`
  - `void updateCategory(String meshId, String category, boolean isType);`
