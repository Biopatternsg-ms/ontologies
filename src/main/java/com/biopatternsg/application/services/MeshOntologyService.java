package com.biopatternsg.application.services;

import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;

import java.util.List;

public interface MeshOntologyService {
    List<String> getMeshTermIds(String objectName);
    SummaryMesh getSummarySynonyms(String meshTermId);
}
