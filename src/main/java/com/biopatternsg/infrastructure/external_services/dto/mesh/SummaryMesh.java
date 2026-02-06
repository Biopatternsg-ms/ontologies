package com.biopatternsg.infrastructure.external_services.dto.mesh;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryMesh {
    private Set<String> parents;
    private Set<String> meshSynonyms;
}
