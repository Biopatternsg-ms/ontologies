package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.GeneOntologyTermService;
import com.biopatternsg.infrastructure.mapper.GoTermMapper;
import com.biopatternsg.domain.model.*;
import com.biopatternsg.domain.port.out.external_repositories.GeneOntologyTermRepoWeb;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
public class GeneOntologyTermServiceImpl implements GeneOntologyTermService {

    @Inject
    GeneOntologyTermRepoWeb goOntologyRepoWeb;

    @Inject
    GeneOntologyTermRepository goTermRepository;

    @Inject
    GoTermMapper goTermMapper;

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return goOntologyRepoWeb.getPathsToRoot(goId);
    }

    @Override
    public List<GoTermDTO> getByIdsInWeb(List<String> ids) {
        GoTermResponse response = goOntologyRepoWeb.findGOTerms(ids);
        return goTermMapper.toEntity(response);
    }

    @Override
    public List<GoTermDTO> getByIdsInDB(List<String> ids) {
        return goTermRepository.findByIds(ids).stream().map(goTermMapper::toDTO).toList();
    }

}
