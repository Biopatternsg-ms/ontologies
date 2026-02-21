package com.biopatternsg.domain.port.out.external_repositories;

import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;

import java.util.List;

public interface MeshOntologyRepoWeb {
    List<String> getTermIds(String biologicalObjectName);
    SummaryMesh getSummarySynonyms(String meshTermId);
}
