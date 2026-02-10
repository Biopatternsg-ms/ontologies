package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.in.BuildMeshOntologyTree;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BuildMeshOntologyTreeUseCase implements BuildMeshOntologyTree {

    private final MeshOntologyService meshOntologyService;
    private final MeshOntologyRepository meshOntologyRepository;

    @Override
    public void execute(BiologicalObject biologicalObject) {
        log.info("Start building tree mesh: {}", biologicalObject.getName());

        List<String> meshTermIds = meshOntologyService.getMeshTermIds(biologicalObject.getName());
        List<String> biologicalObjectsSynonyms = biologicalObject.getSynonyms();
        Stack<String> termIdsStack = new Stack<>();

        Optional<MeshInfo> matchTermIdOptional = findFirstMatchTerm(meshTermIds, biologicalObjectsSynonyms, biologicalObject.getName());

        if (matchTermIdOptional.isEmpty()) {
            log.info("No match term found for {}", biologicalObject.getName());
            return;
        }


        MeshInfo matchTermId = matchTermIdOptional.get();
        saveMechTerm(matchTermId);
        matchTermId.getParents().forEach(termIdsStack::push);

        while (!termIdsStack.isEmpty()) {
            String currentTermId = termIdsStack.pop();

            if (isSavedTerm(currentTermId)) {
                log.info("Term {} already exists", currentTermId);
                continue;
            }

            SummaryMesh currentSummaryMesh = meshOntologyService.getSummarySynonyms(currentTermId);
            List<String> currentTermParents = currentSummaryMesh.getParents().stream().toList();

            List<String> currentSynonyms = currentSummaryMesh.getMeshSynonyms().stream().toList();

            MeshInfo meshInfoToSave = MeshInfo.builder()
                    .meshId(currentTermId)
                    .name(currentSynonyms.getFirst())
                    .parents(currentTermParents)
                    .synonyms(currentSynonyms.subList(1, currentSynonyms.size()))
                    .build();

            saveMechTerm(meshInfoToSave);
            log.info("Term {} saved", currentTermId);

            currentTermParents.forEach(termIdsStack::push);
        }


        log.info("End building tree mesh: {}", biologicalObject.getName());
    }

    private void saveMechTerm(MeshInfo matchTermId) {
        MeshTermCollection collection = new MeshTermCollection();
        collection.setMeshId(matchTermId.getMeshId());
        collection.setName(matchTermId.getName());
        collection.setSynonyms(matchTermId.getSynonyms());
        collection.setParents(matchTermId.getParents());

        meshOntologyRepository.save(collection);
    }

    Optional<MeshInfo> findFirstMatchTerm(List<String> meshTermIds, List<String> biologicalObjectsSynonyms, String name) {
        for (String termId : meshTermIds) {
            SummaryMesh summary = meshOntologyService.getSummarySynonyms(termId);
            boolean containSynonym = containSynonym(summary.getMeshSynonyms().stream().toList(), biologicalObjectsSynonyms);

            if (isSavedName(name)) {
                return Optional.empty();
            }

            if (containSynonym){
                List<String> synonyms = summary.getMeshSynonyms().stream().toList();

                return Optional.of(
                        MeshInfo.builder()
                                .meshId(termId)
                                .name(name)
                                .synonyms(synonyms)
                                .parents(summary.getParents().stream().toList())
                                .build()
                );
            }
        }

        return Optional.empty();
    }

    boolean isSavedName(String name) {
        Optional<MeshTermCollection> byName = meshOntologyRepository.findByName(name);
        return byName.isPresent();
    }

    private boolean containSynonym(List<String> synonyms, List<String> biologicalObjectSynonym) {
        if (synonyms == null || biologicalObjectSynonym == null) {
            return false;
        }
        Set<String> lowerCaseBOSynonyms = biologicalObjectSynonym.stream()
                .filter(Objects::nonNull)
                .map(it -> it.toLowerCase().replaceAll("[\\s\\-_]+", ""))
                .collect(Collectors.toSet());

        return synonyms.stream()
                .filter(Objects::nonNull)
                .map(it -> it.toLowerCase().replaceAll("[\\s\\-_]+", ""))
                .anyMatch(lowerCaseBOSynonyms::contains);
    }

    boolean isSavedTerm(String term) {
        Optional<MeshTermCollection> byName = meshOntologyRepository.findByMeshId(term);
        return byName.isPresent();
    }

}
