package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.GeneOntologyTermService;
import com.biopatternsg.infrastructure.mapper.GoTermMapper;
import com.biopatternsg.domain.model.*;
import com.biopatternsg.domain.port.out.external_repositories.GeneOntologyTermRepoWeb;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class GeneOntologyTermServiceImpl implements GeneOntologyTermService {

    private final GeneOntologyTermRepoWeb goOntologyRepoWeb;
    private final GeneOntologyTermRepository goTermRepository;
    private final GoTermMapper goTermMapper;

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return goOntologyRepoWeb.getPathsToRoot(goId);
    }

    @Override
    public List<GoTerm> getByIdsInWeb(List<String> ids) {
        GoTermResponse response = goOntologyRepoWeb.findGOTerms(ids);
        return goTermMapper.toEntity(response);
    }

    @Override
    public List<GoTerm> getByIdsInDB(List<String> ids) {
        return goTermRepository.findByIds(ids).stream().map(goTermMapper::toModel).toList();
    }

}
