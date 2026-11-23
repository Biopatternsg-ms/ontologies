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
import com.biopatternsg.shared.LogSanitizer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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
        String safeMeshId = LogSanitizer.sanitize(initialMeshId);
        Optional<MeshInfo> initialTermOpt = meshOntologyRepository.findByMeshId(initialMeshId);
        if (initialTermOpt.isEmpty()) {
            log.warn("MeSH node {} not found in ontology", safeMeshId);
            return false;
        }

        Map<String, Boolean> savedCategories = initialTermOpt.get().getCategories();
        if (savedCategories != null && savedCategories.containsKey(targetType.name())) {
            boolean cachedVal = savedCategories.get(targetType.name());
            log.info("MeSH node {} already has cached result for type {}: {}", safeMeshId, targetType, cachedVal);
            return cachedVal;
        }

        log.info("Checking if MeSH node {} is of type {}", safeMeshId, targetType);

        AtomicBoolean matched = new AtomicBoolean(false);
        traverseAncestors(initialMeshId, initialTermOpt.get(), term -> {
            if (targetType.matches(term.getMeshId(), term.getName(), term.getSynonyms())) {
                log.info("MeSH node {} matched type {} at term: {} ({})",
                        safeMeshId, targetType, LogSanitizer.sanitize(term.getName()), LogSanitizer.sanitize(term.getMeshId()));
                matched.set(true);
                return false;
            }
            return true;
        });

        boolean isMatch = matched.get();
        if (!isMatch) {
            log.info("MeSH node {} is NOT of type {}", safeMeshId, targetType);
        }
        meshOntologyRepository.updateCategory(initialMeshId, targetType.name(), isMatch);
        return isMatch;
    }

    @Override
    public Map<MeshCategory, Boolean> executeAll(String meshId) {
        if (meshId == null || meshId.isBlank()) {
            log.warn("Invalid parameter: meshId is null or blank");
            return Collections.emptyMap();
        }

        String trimmedId = meshId.trim();
        String safeMeshId = LogSanitizer.sanitize(trimmedId);
        Optional<MeshInfo> existingTermOpt = meshOntologyRepository.findByMeshId(trimmedId);
        if (existingTermOpt.isEmpty()) {
            log.warn("MeSH node {} not found in ontology", safeMeshId);
            return createAllFalseResult();
        }

        MeshInfo term = existingTermOpt.get();
        Map<MeshCategory, Boolean> resultMap = new LinkedHashMap<>();
        Set<MeshCategory> pendingCategories = initializeResults(term.getCategories(), resultMap);

        if (pendingCategories.isEmpty()) {
            log.info("MeSH node {} already has all categories cached, returning saved data: {}", safeMeshId, resultMap);
            return resultMap;
        }

        log.info("Evaluating missing categories {} for MeSH node {}", pendingCategories, safeMeshId);

        traverseAncestors(trimmedId, term, currentTerm -> {
            matchPendingCategories(currentTerm, pendingCategories, resultMap);
            return !pendingCategories.isEmpty();
        });

        Map<String, Boolean> stringResultMap = toStringResultMap(resultMap);
        meshOntologyRepository.updateCategories(trimmedId, stringResultMap);
        log.info("Saved all MeSH categories for node {}: {}", safeMeshId, stringResultMap);
        return resultMap;
    }

    private void traverseAncestors(String startId, MeshInfo startTerm, Function<MeshInfo, Boolean> shouldContinue) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            Optional<MeshInfo> termOpt = currentId.equals(startId)
                    ? Optional.of(startTerm)
                    : meshOntologyRepository.findByMeshId(currentId);

            if (termOpt.isEmpty()) {
                continue;
            }

            MeshInfo currentTerm = termOpt.get();
            if (!shouldContinue.apply(currentTerm)) {
                return;
            }
            enqueueParents(currentTerm, queue, visited);
        }
    }

    private void enqueueParents(MeshInfo term, Queue<String> queue, Set<String> visited) {
        List<String> parents = term.getParents();
        if (parents == null) {
            return;
        }
        for (String parent : parents) {
            if (parent == null || parent.isBlank()) {
                continue;
            }
            String trimmedParent = parent.trim();
            if (!visited.contains(trimmedParent) && !MeshCategory.isRootTerm(trimmedParent)) {
                visited.add(trimmedParent);
                queue.add(trimmedParent);
            }
        }
    }

    private Map<MeshCategory, Boolean> createAllFalseResult() {
        Map<MeshCategory, Boolean> notFoundResult = new LinkedHashMap<>();
        for (MeshCategory cat : MeshCategory.values()) {
            notFoundResult.put(cat, false);
        }
        return notFoundResult;
    }

    private Set<MeshCategory> initializeResults(Map<String, Boolean> savedCategories, Map<MeshCategory, Boolean> resultMap) {
        Set<MeshCategory> pendingCategories = EnumSet.noneOf(MeshCategory.class);
        for (MeshCategory category : MeshCategory.values()) {
            if (savedCategories != null && savedCategories.containsKey(category.name())) {
                resultMap.put(category, savedCategories.get(category.name()));
            } else {
                resultMap.put(category, false);
                pendingCategories.add(category);
            }
        }
        return pendingCategories;
    }

    private void matchPendingCategories(MeshInfo currentTerm, Set<MeshCategory> pendingCategories, Map<MeshCategory, Boolean> resultMap) {
        Iterator<MeshCategory> it = pendingCategories.iterator();
        while (it.hasNext()) {
            MeshCategory category = it.next();
            if (category.matches(currentTerm.getMeshId(), currentTerm.getName(), currentTerm.getSynonyms())) {
                resultMap.put(category, true);
                it.remove();
            }
        }
    }

    private Map<String, Boolean> toStringResultMap(Map<MeshCategory, Boolean> resultMap) {
        Map<String, Boolean> stringResultMap = new LinkedHashMap<>();
        for (Map.Entry<MeshCategory, Boolean> entry : resultMap.entrySet()) {
            stringResultMap.put(entry.getKey().name(), entry.getValue());
        }
        return stringResultMap;
    }
}
