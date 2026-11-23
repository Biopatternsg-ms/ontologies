# Quickstart & Validation Guide: Use Cases Lightweight Orchestration

**Feature**: `002-usecase-services-encapsulation`  
**Date**: 2026-11-23

## Prerequisites

- Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven Wrapper (`./mvnw`) in `services/ontologies`

## Validation Scenarios

### 1. Compile & Architecture Boundary Checks
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test -Dtest=ArchitectureLayerTest
```
**Expected Outcome**: All ArchUnit layer isolation and naming convention tests pass.

### 2. Full Test Suite (Unit & Controller Tests)
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test
```
**Expected Outcome**: All unit tests (`CheckMeshTermTypeUseCaseTest`, `SearchMeshIdBySynonymsUseCaseTest`, `MeshCategoryTest`, `BuildMeshOntologyTreeUseCaseTest`, `SendBiologicalObjectToQueueUseCaseTest`, etc.) pass with zero failures.

### 3. Static Verification of Removed Anti-Patterns
```bash
grep -rn "java.util.Stack\|saveMechTerm\|BiologicalObjectsServiceImp\b" src/
```
**Expected Outcome**: Zero matches (exit code 1).
