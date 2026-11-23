# Quickstart & Validation Guide: Architecture & Documentation Governance

**Feature**: `005-arch-doc-governance`  
**Date**: 2026-11-23

## Prerequisites

- Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven Wrapper (`./mvnw`) in `services/ontologies`

## Validation Scenarios

### 1. Run Architecture & Documentation Governance Tests
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test -Dtest=ArchitectureLayerTest,DocumentationGovernanceTest
```
**Expected Outcome**: All ArchUnit rules and documentation governance assertions pass with zero errors.

### 2. Full Test Suite
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test
```
**Expected Outcome**: All unit, controller, architecture, and documentation governance tests pass.
