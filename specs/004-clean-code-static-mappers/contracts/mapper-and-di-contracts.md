# Static Mapper & Dependency Injection Contracts

**Feature**: `004-clean-code-static-mappers`  
**Date**: 2026-11-23

## 1. `GoTermMapper` (`com.biopatternsg.infrastructure.mapper`)
- `public final class GoTermMapper` (private constructor, zero CDI annotations)
- `public static List<GoTerm> quickGoToModelList(GoTermResponse response)`
- `public static GoTerm collectionToModel(GoTermCollection entity)`
- `public static GoTermCollection modelToCollection(GoTerm model)`
- `public static PathsToRoot pathsToRootToModel(PathsToRootResponse response)`

## 2. `MeshTermMapper` (`com.biopatternsg.infrastructure.mapper`)
- `public final class MeshTermMapper` (private constructor, zero CDI annotations)
- `public static MeshInfo collectionToModel(MeshTermCollection entity)`
- `public static MeshTermCollection modelToCollection(MeshInfo model)`

## 3. Constructor Injection Contracts
- `MeshQueueSenderRabbitAdapter(@Channel("ncbi-out") Emitter<BiologicalObject> emitter)`
- `QueryGoImpl(@RestClient GeneOntologyHttpClient goHttpClient)` (zero field-level `@Inject` / `@RestClient`)
- `GOAdapter(QueryGO queryGO)` (zero `GoTermMapper` field)
- `GeneOntologyTermRepositoryAdapter()` (zero `GoTermMapper` field)
