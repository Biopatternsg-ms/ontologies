package com.biopatternsg.infrastructure.external_services.impl;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.clients.external_clients.GeneOntologyHttpClient;
import com.biopatternsg.infrastructure.external_services.QueryGO;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class QueryGoImpl implements QueryGO {

    @RestClient
    @Inject
    private final GeneOntologyHttpClient goHttpClient;

    public QueryGoImpl(@RestClient GeneOntologyHttpClient goHttpClient) {
        this.goHttpClient = goHttpClient;
    }

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return goHttpClient.getPathsToRoot(goId);
    }

    @Override
    public GoTermResponse findGeneOntologies(String joinIds) {
        return goHttpClient.findGeneOntologies(joinIds);
    }
}
