package com.biopatternsg.infrastructure.external_services;

import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;

import java.util.List;

public interface QueryMesh {
    List<String> getTermIds(String biologicalObjectName);
    SummaryMesh getSummarySynonyms(String meshTermId);
}
