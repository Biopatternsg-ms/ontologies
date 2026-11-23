# Quickstart & Validation Guide: Domain Layer Purity

**Feature**: `001-domain-layer-purity`  
**Date**: 2026-11-23

## Prerequisites

- Java 21
- Maven Wrapper (`./mvnw`) in `services/ontologies`

## Validation Scenarios

### 1. Compile & Refresh Bytecode
```bash
./mvnw clean compile
```
**Expected Outcome**: Build succeeds with zero compilation errors across `domain`, `application`, and `infrastructure`.

### 2. Verify Hexagonal Layer Isolation & Naming Rules (ArchUnit)
```bash
./mvnw test -Dtest=ArchitectureLayerTest
```
**Expected Outcome**: All ArchUnit tests pass, including domain/application layer isolation and framework-decoupling rules.

### 3. Verify Full Test Suite (Unit & Controller Tests)
```bash
./mvnw test
```
**Expected Outcome**: All unit tests (including `ESummaryResult` null-safety tests and `GoTermMapper` tests) and controller tests pass with zero failures or errors.

### 4. Static Import Verification
```bash
grep -rnE "jakarta\.ws\.rs|io\.vertx|io\.quarkus|jakarta\.xml\.bind|com\.fasterxml\.jackson|org\.bson|org\.w3c\.dom" src/main/java/com/biopatternsg/domain/
grep -rnE "jakarta\.ws\.rs|io\.vertx" src/main/java/com/biopatternsg/application/
```
**Expected Outcome**: Both commands return zero matches (exit code 1).
