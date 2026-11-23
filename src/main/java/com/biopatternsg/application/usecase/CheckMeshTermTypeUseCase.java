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

import com.biopatternsg.domain.model.mesh.MeshCategory;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.in.CheckMeshTermType;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class CheckMeshTermTypeUseCase implements CheckMeshTermType {

    private final MeshOntologyRepository meshOntologyRepository;

    @Override
    public boolean execute(String meshId, MeshCategory targetType) {
        if (meshId == null || meshId.isBlank() || targetType == null) {
            log.warn("Invalid parameters: meshId or targetType is null/blank");
            return false;
        }

        String initialMeshId = meshId.trim();

        Optional<MeshInfo> initialTermOpt = meshOntologyRepository.findByMeshId(initialMeshId);
        if (initialTermOpt.isEmpty()) {
            log.warn("MeSH node {} not found in ontology", initialMeshId);
            return false;
        }

        Map<String, Boolean> savedCategories = initialTermOpt.get().getCategories();
        if (savedCategories != null && savedCategories.containsKey(targetType.name())) {
            boolean cachedVal = savedCategories.get(targetType.name());
            log.info("MeSH node {} already has cached result for type {}: {}", initialMeshId, targetType, cachedVal);
            return cachedVal;
        }

        log.info("Checking if MeSH node {} is of type {}", initialMeshId, targetType);

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(initialMeshId);
        visited.add(initialMeshId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            Optional<MeshInfo> termOpt = currentId.equals(initialMeshId)
                    ? initialTermOpt
                    : meshOntologyRepository.findByMeshId(currentId);

            if (termOpt.isPresent()) {
                MeshInfo term = termOpt.get();

                if (targetType.matches(term.getMeshId(), term.getName(), term.getSynonyms())) {
                    log.info("MeSH node {} matched type {} at term: {} ({})", initialMeshId, targetType, term.getName(), term.getMeshId());
                    meshOntologyRepository.updateCategory(initialMeshId, targetType.name(), true);
                    return true;
                }

                List<String> parents = term.getParents();
                if (parents != null) {
                    for (String parent : parents) {
                        if (parent != null && !parent.isBlank()) {
                            String trimmedParent = parent.trim();
                            if (!visited.contains(trimmedParent) && !MeshCategory.isRootTerm(trimmedParent)) {
                                visited.add(trimmedParent);
                                queue.add(trimmedParent);
                            }
                        }
                    }
                }
            }
        }

        log.info("MeSH node {} is NOT of type {}", initialMeshId, targetType);
        meshOntologyRepository.updateCategory(initialMeshId, targetType.name(), false);
        return false;
    }

    @Override
    public Map<MeshCategory, Boolean> executeAll(String meshId) {
        if (meshId == null || meshId.isBlank()) {
            log.warn("Invalid parameter: meshId is null or blank");
            return Collections.emptyMap();
        }

        String trimmedId = meshId.trim();

        Optional<MeshInfo> existingTermOpt = meshOntologyRepository.findByMeshId(trimmedId);
        if (existingTermOpt.isEmpty()) {
            log.warn("MeSH node {} not found in ontology", trimmedId);
            Map<MeshCategory, Boolean> notFoundResult = new LinkedHashMap<>();
            for (MeshCategory cat : MeshCategory.values()) {
                notFoundResult.put(cat, false);
            }
            return notFoundResult;
        }

        MeshInfo term = existingTermOpt.get();
        Map<String, Boolean> savedCategories = term.getCategories();

        Map<MeshCategory, Boolean> resultMap = new LinkedHashMap<>();
        Set<MeshCategory> pendingCategories = EnumSet.noneOf(MeshCategory.class);

        for (MeshCategory category : MeshCategory.values()) {
            if (savedCategories != null && savedCategories.containsKey(category.name())) {
                resultMap.put(category, savedCategories.get(category.name()));
            } else {
                resultMap.put(category, false);
                pendingCategories.add(category);
            }
        }

        if (pendingCategories.isEmpty()) {
            log.info("MeSH node {} already has all categories cached, returning saved data: {}", trimmedId, resultMap);
            return resultMap;
        }

        log.info("Evaluating missing categories {} for MeSH node {}", pendingCategories, trimmedId);

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(trimmedId);
        visited.add(trimmedId);

        while (!queue.isEmpty() && !pendingCategories.isEmpty()) {
            String currentId = queue.poll();
            Optional<MeshInfo> termOpt = currentId.equals(trimmedId)
                    ? existingTermOpt
                    : meshOntologyRepository.findByMeshId(currentId);

            if (termOpt.isPresent()) {
                MeshInfo currentTerm = termOpt.get();

                Iterator<MeshCategory> it = pendingCategories.iterator();
                while (it.hasNext()) {
                    MeshCategory category = it.next();
                    if (category.matches(currentTerm.getMeshId(), currentTerm.getName(), currentTerm.getSynonyms())) {
                        resultMap.put(category, true);
                        it.remove();
                    }
                }

                if (pendingCategories.isEmpty()) {
                    break;
                }

                List<String> parents = currentTerm.getParents();
                if (parents != null) {
                    for (String parent : parents) {
                        if (parent != null && !parent.isBlank()) {
                            String trimmedParent = parent.trim();
                            if (!visited.contains(trimmedParent) && !MeshCategory.isRootTerm(trimmedParent)) {
                                visited.add(trimmedParent);
                                queue.add(trimmedParent);
                            }
                        }
                    }
                }
            }
        }

        Map<String, Boolean> stringResultMap = new LinkedHashMap<>();
        for (Map.Entry<MeshCategory, Boolean> entry : resultMap.entrySet()) {
            stringResultMap.put(entry.getKey().name(), entry.getValue());
        }

        meshOntologyRepository.updateCategories(trimmedId, stringResultMap);
        log.info("Saved all MeSH categories for node {}: {}", trimmedId, stringResultMap);
        return resultMap;
    }
}