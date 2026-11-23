# Quickstart & Validation Guide: Delivery Layer Compliance

**Feature**: `003-adapters-delivery-compliance`  
**Date**: 2026-11-23

## Prerequisites

- Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven Wrapper (`./mvnw`) in `services/ontologies`

## Validation Scenarios

### 1. Full Build & Test Suite
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test
```
**Expected Outcome**: `MeshOntologyControllerTest`, `GeneOntologyControllerTest`, `OntologyRequestMapperTest`, and `ArchitectureLayerTest` all pass with zero errors.

### 2. Static Verification of Removed Anti-Patterns
```bash
grep -rn "Buildind\|buildMeshOntologyTree\|mesh MeshId\|domain\.model\." src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/ src/main/java/com/biopatternsg/infrastructure/clients/internal_clients/ src/main/java/com/biopatternsg/infrastructure/internal_services/impl/
```
**Expected Outcome**: Zero matches of `Buildind`, `buildMeshOntologyTree`, `mesh MeshId`, or `domain.model.` in REST controller request parameter bindings (note: `MeshCategory` in `MeshOntologyController` is an enum used in `CheckMeshTypeResponse` / `Map<MeshCategory, Boolean>`, not a request body binding).
