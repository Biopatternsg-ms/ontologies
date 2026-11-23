/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.GeneOntologyTermService;
import com.biopatternsg.domain.model.GeneOntologyBuildTreeRequest;
import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.model.PathToRoot;
import com.biopatternsg.domain.port.in.BuildGeneOntologyTree;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BuildGeneOntologyTreeUseCase implements BuildGeneOntologyTree {

    private final GeneOntologyTermService geneOntologyTermService;
    private final GeneOntologyTermRepository goTermRepository;

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
            goTermRepository.save(goTerm);
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
