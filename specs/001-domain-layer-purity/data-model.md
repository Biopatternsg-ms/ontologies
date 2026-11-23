# Data Model: Domain Layer Purity and Framework Decoupling

**Feature**: `001-domain-layer-purity`  
**Date**: 2026-11-23

## 1. Pure Domain Models (`com.biopatternsg.domain.model`)

### `PathsToRoot` (Updated — Framework-Free)
- **Package**: `com.biopatternsg.domain.model`
- **Annotations**: Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` (includes explicit `toString()` via `@Data`, zero Jackson annotations).
- **Fields**:
  - `int numberOfHits`
  - `List<List<PathToRoot>> results`

### `PathToRoot` (Updated — Framework-Free)
- **Package**: `com.biopatternsg.domain.model`
- **Annotations**: Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` (includes explicit `toString()` via `@Data`, zero Jackson annotations).
- **Fields**:
  - `String child`
  - `String parent`
  - `String relationship`

## 2. Infrastructure External-Service DTOs

### `PathsToRootResponse` & `PathToRootResponse` (New — Java Records)
- **Package**: `com.biopatternsg.infrastructure.external_services.dto.go`
- **Annotations**: `@JsonIgnoreProperties(ignoreUnknown = true)`
- **Structure**:
  - `public record PathsToRootResponse(int numberOfHits, List<List<PathToRootResponse>> results)`
  - `public record PathToRootResponse(String child, String parent, String relationship)`
- **Mapping**: Converted to `PathsToRoot` / `PathToRoot` in `GoTermMapper.toPathsToRoot(PathsToRootResponse)`. Null `response` returns `null` (or empty `PathsToRoot` if `results` is null, preserving null-safety for callers).

### `ESearchResult` (Relocated)
- **Old Package**: `com.biopatternsg.domain.model.mesh`
- **New Package**: `com.biopatternsg.infrastructure.external_services.dto.mesh`
- **Role**: JAXB/Jackson XML DTO for NCBI E-utilities `esearch.fcgi` response (`IdList` -> `List<String> ids`).

### `ESummaryResult` (Relocated & Cleaned)
- **Old Package**: `com.biopatternsg.domain.model.mesh`
- **New Package**: `com.biopatternsg.infrastructure.external_services.dto.mesh`
- **Role**: JAXB/Jackson/Quarkus reflection XML DTO for NCBI E-utilities `esummary.fcgi` response (`DocSum` -> `id`, `List<Item> items`).
- **Methods on `ESummaryResult.Item`**:
  - `getValuesBySubItemName(String subItemName)`: Returns `List<String>` of trimmed text content for matching `<Item Name="...">` child elements; null-safe on `content` and `child.getTextContent()`.
  - `getValuesBySubItemNameOrText(String subItemName)` *(renamed from `getValuesBySubItemNameOtra`)*: Returns `List<String>` of trimmed text content for matching child `<Item Name="...">` elements, or the element's own trimmed text when no child `<Item>` elements exist; null-safe on `content` and `getTextContent()`.
  - `getSubItemValues()`: **Removed** (unused dead method with NPE risk on `getFirstChild()`).

## 3. Removed Domain Artifacts

- `com.biopatternsg.domain.port.out.consumers.MeshQueueConsumer`: **Deleted**. `MeshQueueConsumerRabbitImpl` remains in `com.biopatternsg.infrastructure.adapters.in.consumers` as a standalone inbound RabbitMQ adapter invoking `BuildMeshOntologyTree`.
