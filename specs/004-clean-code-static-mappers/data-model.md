# Data Model: Clean Code Lambdas, Dependency Injection, Null Safety and Static Mappers

**Feature**: `004-clean-code-static-mappers`  
**Date**: 2026-11-23

## 1. Domain Models (`com.biopatternsg.domain.model.*`)

### `GeneOntologyBuildTreeRequest`
- **Annotations**: `@AllArgsConstructor`, `@Getter`, `@Setter`, `@ToString`
- **`getGoTermIds()`**: Safely handles `null` lists and `null` elements via `Objects::nonNull`, returning distinct GO term IDs via `.distinct().toList()`.

### `MeshInfo` (`com.biopatternsg.domain.model.mesh`)
- **Annotations**: `@Builder`, `@Getter`, `@Setter`, `@ToString`

## 2. Relocated External DTOs (`com.biopatternsg.infrastructure.external_services.dto.mesh`)

- `ESearchResult` & `ESearchResult.IdListWrapper`: annotated with `@ToString`.
- `ESummaryResult`, `ESummaryResult.DocSum`, & `ESummaryResult.Item`: annotated with `@ToString`.
