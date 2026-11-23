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

import com.biopatternsg.application.services.BiologicalObjectNameResolver;
import com.biopatternsg.application.services.BiologicalObjectsService;
import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.model.mesh.SummaryMesh;
import com.biopatternsg.domain.port.in.BuildMeshOntologyTree;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.shared.LogSanitizer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BuildMeshOntologyTreeUseCase implements BuildMeshOntologyTree {

    private final MeshOntologyService meshOntologyService;
    private final MeshOntologyRepository meshOntologyRepository;
    private final BiologicalObjectsService biologicalObjectsService;

    @Override
    public void execute(BiologicalObject biologicalObject) {
        if (biologicalObject == null) {
            log.warn("Received null BiologicalObject in BuildMeshOntologyTreeUseCase");
            return;
        }

        Optional<String> effectiveNameOpt = BiologicalObjectNameResolver.resolveEffectiveName(biologicalObject);
        if (effectiveNameOpt.isEmpty()) {
            log.warn("Skipping building tree mesh for biologicalObject id={}: name and symbol are null or blank",
                    LogSanitizer.sanitize(biologicalObject.getId()));
            return;
        }

        String effectiveName = effectiveNameOpt.get();
        String safeEffectiveName = LogSanitizer.sanitize(effectiveName);
        if (isSavedName(effectiveName)) {
            log.info("Mesh tree for {} already exists, skipping external lookup", safeEffectiveName);
            return;
        }

        log.info("Start building tree mesh: {}", safeEffectiveName);

        List<String> meshTermIds = meshOntologyService.getMeshTermIds(effectiveName);
        Optional<MeshInfo> matchTermOptional = findFirstMatchTerm(meshTermIds, biologicalObject.getSynonyms(), effectiveName);
        if (matchTermOptional.isEmpty()) {
            log.info("No match term found for {}", safeEffectiveName);
            return;
        }

        MeshInfo matchTerm = matchTermOptional.get();
        saveMeshTerm(matchTerm);
        biologicalObjectsService.updateMeshId(biologicalObject.getId(), matchTerm.getMeshId());
        saveAncestorTree(matchTerm);

        log.info("End building tree mesh: {}", safeEffectiveName);
    }

    private void saveAncestorTree(MeshInfo rootMatch) {
        Deque<String> termIdsQueue = new ArrayDeque<>();
        if (rootMatch.getParents() != null) {
            rootMatch.getParents().forEach(termIdsQueue::push);
        }

        while (!termIdsQueue.isEmpty()) {
            String currentTermId = termIdsQueue.pop();
            String safeTermId = LogSanitizer.sanitize(currentTermId);
            if (isSavedTerm(currentTermId)) {
                log.info("Term {} already exists", safeTermId);
                continue;
            }

            SummaryMesh currentSummaryMesh = meshOntologyService.getSummarySynonyms(currentTermId);
            MeshInfo ancestorMeshInfo = buildAncestorMeshInfo(currentTermId, currentSummaryMesh);
            saveMeshTerm(ancestorMeshInfo);
            log.info("Term {} saved", safeTermId);

            ancestorMeshInfo.getParents().forEach(termIdsQueue::push);
        }
    }

    private MeshInfo buildAncestorMeshInfo(String termId, SummaryMesh summary) {
        List<String> parents = summary.getParents().stream().toList();
        List<String> synonyms = summary.getMeshSynonyms().stream().toList();
        String name = synonyms.isEmpty() ? termId : synonyms.getFirst();
        List<String> remainingSynonyms = synonyms.size() > 1
                ? synonyms.subList(1, synonyms.size())
                : Collections.emptyList();

        return MeshInfo.builder()
                .meshId(termId)
                .name(name)
                .parents(parents)
                .synonyms(remainingSynonyms)
                .build();
    }

    private void saveMeshTerm(MeshInfo meshInfo) {
        meshOntologyRepository.save(meshInfo);
    }

    private Optional<MeshInfo> findFirstMatchTerm(List<String> meshTermIds, List<String> biologicalObjectsSynonyms, String name) {
        for (String termId : meshTermIds) {
            SummaryMesh summary = meshOntologyService.getSummarySynonyms(termId);
            List<String> synonyms = summary.getMeshSynonyms().stream().toList();
            if (containSynonym(synonyms, biologicalObjectsSynonyms)) {
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

    private boolean isSavedName(String name) {
        return meshOntologyRepository.findByName(name).isPresent();
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

    private boolean isSavedTerm(String term) {
        return meshOntologyRepository.findByMeshId(term).isPresent();
    }
}
