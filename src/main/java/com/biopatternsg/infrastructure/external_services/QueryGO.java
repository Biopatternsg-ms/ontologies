package com.biopatternsg.infrastructure.external_services;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;

public interface QueryGO {
    PathsToRoot getPathsToRoot(String goId);
    GoTermResponse findGeneOntologies(String joinIds);
}
