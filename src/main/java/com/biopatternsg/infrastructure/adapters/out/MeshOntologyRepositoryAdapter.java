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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import org.bson.conversions.Bson;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

        List<Bson> filters = synonyms.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> {
                    Pattern pattern = Pattern.compile("^" + Pattern.quote(s) + "$", Pattern.CASE_INSENSITIVE);
                    return Stream.of(
                            Filters.regex("name", pattern),
                            Filters.regex("synonyms", pattern)
                    );
                })
                .toList();

        if (filters.isEmpty()) {
            return Optional.empty();
        }

        return find(Filters.or(filters)).firstResultOptional().map(this::toModel);
    }

    @Override
    public void updateCategories(String meshId, Map<String, Boolean> categories) {
        if (meshId == null || meshId.isBlank() || categories == null) {
            return;
        }
        mongoCollection().updateOne(
                Filters.eq("meshId", meshId.trim()),
                Updates.set("categories", categories)
        );
    }

    @Override
    public void updateCategory(String meshId, String category, boolean isType) {
        if (meshId == null || meshId.isBlank() || category == null || category.isBlank()) {
            return;
        }
        mongoCollection().updateOne(
                Filters.eq("meshId", meshId.trim()),
                Updates.set("categories." + category.trim(), isType)
        );
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
                .categories(entity.getCategories())
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
        entity.setCategories(model.getCategories());
        return entity;
    }
}
