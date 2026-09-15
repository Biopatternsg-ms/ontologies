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
        log.info("Checking if MeSH node {} is of type {}", initialMeshId, targetType);

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(initialMeshId);
        visited.add(initialMeshId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            Optional<MeshInfo> termOpt = meshOntologyRepository.findByMeshId(currentId);

            if (termOpt.isPresent()) {
                MeshInfo term = termOpt.get();

                if (targetType.matches(term.getMeshId(), term.getName(), term.getSynonyms())) {
                    log.info("MeSH node {} matched type {} at term: {} ({})", initialMeshId, targetType, term.getName(), term.getMeshId());
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
        return false;
    }
}