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
package com.biopatternsg.infrastructure.adapters.out;

import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class MeshOntologyRepositoryAdapter implements MeshOntologyRepository, PanacheMongoRepository<MeshTermCollection> {

    @Override
    public void save(MeshInfo meshInfo) {
        persistOrUpdate(toEntity(meshInfo));
    }

    @Override
    public Optional<MeshInfo> findByMeshId(String meshId) {
        return find("{meshId: ?1}", meshId).firstResultOptional().map(this::toModel);
    }

    @Override
    public Optional<MeshInfo> findByName(String name) {
        return find("{name: ?1}", name).firstResultOptional().map(this::toModel);
    }

    @Override
    public Optional<MeshInfo> findBySynonyms(List<String> synonyms) {
        return find("{$or: [{synonyms: {$in: ?1}}, {name: {$in: ?1}}]}", synonyms).firstResultOptional().map(this::toModel);
    }

    @Override
    public Optional<MeshInfo> findBySynonymsCaseInsensitive(List<String> synonyms) {
        if (synonyms == null || synonyms.isEmpty()) {
            return Optional.empty();
        }

        List<Pattern> regexList = synonyms.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> Pattern.compile("^" + Pattern.quote(s) + "$", Pattern.CASE_INSENSITIVE))
                .toList();

        if (regexList.isEmpty()) {
            return Optional.empty();
        }

        return find("{$or: [{synonyms: {$in: ?1}}, {name: {$in: ?1}}]}", regexList).firstResultOptional().map(this::toModel);
    }

    private MeshInfo toModel(MeshTermCollection entity) {
        if (entity == null) {
            return null;
        }
        return MeshInfo.builder()
                .meshId(entity.getMeshId())
                .name(entity.getName())
                .synonyms(entity.getSynonyms())
                .parents(entity.getParents())
                .build();
    }

    private MeshTermCollection toEntity(MeshInfo model) {
        if (model == null) {
            return null;
        }
        MeshTermCollection entity = new MeshTermCollection();
        entity.setMeshId(model.getMeshId());
        entity.setName(model.getName());
        entity.setSynonyms(model.getSynonyms());
        entity.setParents(model.getParents());
        return entity;
    }
}
