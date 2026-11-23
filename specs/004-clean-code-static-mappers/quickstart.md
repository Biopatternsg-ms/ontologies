# Quickstart & Validation Guide: Clean Code, DI & Static Mappers

**Feature**: `004-clean-code-static-mappers`  
**Date**: 2026-11-23

## Prerequisites

- Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven Wrapper (`./mvnw`) in `services/ontologies`

## Validation Scenarios

### 1. Full Build & Test Suite
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test
```
**Expected Outcome**: All unit tests (`GoTermMapperTest`, `MeshTermMapperTest`, `GeneOntologyBuildTreeRequestTest`, `ArchitectureLayerTest`, etc.) pass with zero errors.

### 2. Static Verification of Removed Anti-Patterns
```bash
grep -rn "Collectors\.toList()" src/main/java/
grep -rn "@Inject" src/main/java/com/biopatternsg/application/ src/main/java/com/biopatternsg/infrastructure/adapters/
```
**Expected Outcome**: Zero matches for `Collectors.toList()` and zero `@Inject` field injections in `application.*` or `infrastructure.adapters.*`.
