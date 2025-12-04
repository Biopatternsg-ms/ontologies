package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.GeneOntologyTermService;
import com.biopatternsg.infrastructure.mapper.GoTermMapper;
import com.biopatternsg.domain.model.*;
import com.biopatternsg.domain.port.in.BuildGeneOntologyTree;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BuildGeneOntologyTreeUseCase implements BuildGeneOntologyTree {

    private final GeneOntologyTermService geneOntologyTermService;

    private final GeneOntologyTermRepository goTermRepository;

    private final GoTermMapper goTermMapper;

    @Override
    public void execute(GeneOntologyBuildTreeRequest geneOntology) {
        log.info("Start Building gene ontology tree");

        List<String> newGOTermIds = getNewGOTermIds(geneOntology);

        log.info("Total by build gene ontology tree: {}", newGOTermIds.size());

        if (newGOTermIds.isEmpty()) {
            log.info("End Building gene ontology tree");
            return;
        }

        List<GoTerm> newGoTerms = geneOntologyTermService.getByIdsInWeb(newGOTermIds);

        newGoTerms.forEach(goTerm -> {
            List<GoTerm.ParentRelation> parentRelations = getParentRelations(goTerm.getTermId());
            goTerm.setParentRelations(parentRelations);
            goTermRepository.save(goTermMapper.toEntity(goTerm));
        });

        log.info("End Building gene ontology tree");
    }

    private List<GoTerm.ParentRelation> getParentRelations(String newGoTermId) {
        List<List<PathToRoot>> newGoTermPathsToRoots = geneOntologyTermService.getPathsToRoot(newGoTermId).getResults();

        Set<PathToRoot> flatNewGoTermPathsToRoots = newGoTermPathsToRoots.stream()
                .flatMap(List::stream)
                .filter(pathToRoot -> pathToRoot.getChild().equals(newGoTermId))
                .collect(Collectors.toSet());

        return flatNewGoTermPathsToRoots.stream()
                .map(a -> new GoTerm.ParentRelation(a.getParent(), a.getRelationship()))
                .toList();
    }

    private List<String> getNewGOTermIds(GeneOntologyBuildTreeRequest geneOntology) {
        List<String> goIdsOfRequest = geneOntology.getGoTermIds();
        List<GoTerm> goTermEntities = geneOntologyTermService.getByIdsInDB(goIdsOfRequest);
        List<String> termIdsInDB = goTermEntities.stream().map(GoTerm::getTermId).toList();

        return goIdsOfRequest.stream().filter(id -> !termIdsInDB.contains(id)).toList();
    }
}

