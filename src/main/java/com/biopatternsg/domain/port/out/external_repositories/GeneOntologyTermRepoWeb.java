package com.biopatternsg.domain.port.out.external_repositories;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;

import java.util.List;

public interface GeneOntologyTermRepoWeb {
    PathsToRoot getPathsToRoot(String goId);
    GoTermResponse findGOTerms(List<String> goTermIds);
}
