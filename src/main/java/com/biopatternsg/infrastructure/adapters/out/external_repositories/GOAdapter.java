package com.biopatternsg.infrastructure.adapters.out.external_repositories;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.domain.port.out.external_repositories.GeneOntologyTermRepoWeb;
import com.biopatternsg.infrastructure.external_services.QueryGO;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GOAdapter implements GeneOntologyTermRepoWeb {

    private final QueryGO queryGO;

    public GOAdapter(QueryGO queryGO) {
        this.queryGO = queryGO;
    }

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return queryGO.getPathsToRoot(goId);
    }

    @Override
    public GoTermResponse findGOTerms(List<String> goTermIds) {
        String joinIds = String.join(",", goTermIds);
        return queryGO.findGeneOntologies(joinIds);
    }
}
