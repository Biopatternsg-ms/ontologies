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

import com.biopatternsg.domain.port.in.SearchMeshIdBySynonyms;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SearchMeshIdBySynonymsUseCase implements SearchMeshIdBySynonyms {

    private final MeshOntologyRepository meshOntologyRepository;

    @Override
    public Optional<String> execute(List<String> synonyms) {
        if (synonyms == null || synonyms.isEmpty()) {
            log.warn("Synonyms list provided for MeSH search is empty or null");
            return Optional.empty();
        }

        List<String> cleanSynonyms = synonyms.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleanSynonyms.isEmpty()) {
            log.warn("No valid terms found after cleaning synonyms list");
            return Optional.empty();
        }

        log.info("Searching MeSH ID for synonyms: {}", cleanSynonyms);

        Optional<MeshTermCollection> matchOptional = meshOntologyRepository.findBySynonymsCaseInsensitive(cleanSynonyms);

        if (matchOptional.isPresent()) {
            String meshId = matchOptional.get().getMeshId();
            log.info("Found matching MeSH ID: {} for name: {}", meshId, matchOptional.get().getName());
            return Optional.of(meshId);
        }

        log.info("No matching MeSH term found for provided synonyms");
        return Optional.empty();
    }
}