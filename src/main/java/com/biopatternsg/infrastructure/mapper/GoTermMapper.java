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
package com.biopatternsg.infrastructure.mapper;

import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.model.PathToRoot;
import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import com.biopatternsg.infrastructure.external_services.dto.go.PathToRootResponse;
import com.biopatternsg.infrastructure.external_services.dto.go.PathsToRootResponse;
import com.biopatternsg.infrastructure.mongo.GoTermCollection;

import java.util.List;
import java.util.Objects;

public final class GoTermMapper {

    private GoTermMapper() {
    }

    public static List<GoTerm> quickGoToModelList(GoTermResponse response) {
        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapQuickGoTermToModel)
                .toList();
    }

    public static GoTerm collectionToModel(GoTermCollection entity) {
        if (entity == null) {
            return null;
        }
        return GoTerm.builder()
                .id(entity.id != null ? entity.id.toString() : null)
                .termId(entity.getTermId())
                .name(entity.getName())
                .synonyms(mapSynonymsToModel(entity.getSynonyms()))
                .parentRelations(mapParentRelationsToModel(entity.getParentRelations()))
                .build();
    }

    public static GoTermCollection modelToCollection(GoTerm model) {
        if (model == null) {
            return null;
        }
        var entity = new GoTermCollection();
        entity.setTermId(model.getTermId());
        entity.setName(model.getName());
        entity.setSynonyms(mapModelToSynonyms(model.getSynonyms()));
        entity.setParentRelations(mapModelToParentRelations(model.getParentRelations()));
        return entity;
    }

    public static PathsToRoot pathsToRootToModel(PathsToRootResponse response) {
        if (response == null) {
            return PathsToRoot.builder()
                    .numberOfHits(0)
                    .results(List.of())
                    .build();
        }
        List<List<PathToRoot>> mappedResults = response.results() == null
                ? List.of()
                : response.results().stream()
                        .filter(Objects::nonNull)
                        .map(GoTermMapper::mapPathGroup)
                        .toList();
        return PathsToRoot.builder()
                .numberOfHits(response.numberOfHits())
                .results(mappedResults)
                .build();
    }

    private static List<GoTerm.Synonym> mapSynonymsToModel(List<GoTermCollection.Synonym> synonyms) {
        if (synonyms == null) {
            return List.of();
        }
        return synonyms.stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapSynonymToModel)
                .toList();
    }

    private static GoTerm.Synonym mapSynonymToModel(GoTermCollection.Synonym syn) {
        return GoTerm.Synonym.builder()
                .name(syn.getName())
                .type(syn.getType())
                .build();
    }

    private static List<GoTermCollection.Synonym> mapModelToSynonyms(List<GoTerm.Synonym> synonyms) {
        if (synonyms == null) {
            return List.of();
        }
        return synonyms.stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapSynonymToCollection)
                .toList();
    }

    private static GoTermCollection.Synonym mapSynonymToCollection(GoTerm.Synonym syn) {
        var synonym = new GoTermCollection.Synonym();
        synonym.setName(syn.getName());
        synonym.setType(syn.getType());
        return synonym;
    }

    private static List<GoTerm.ParentRelation> mapParentRelationsToModel(List<GoTermCollection.ParentRelation> relations) {
        if (relations == null) {
            return List.of();
        }
        return relations.stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapParentRelationToModel)
                .toList();
    }

    private static GoTerm.ParentRelation mapParentRelationToModel(GoTermCollection.ParentRelation rel) {
        return GoTerm.ParentRelation.builder()
                .parent(rel.getParent())
                .relationship(rel.getRelationship())
                .build();
    }

    private static List<GoTermCollection.ParentRelation> mapModelToParentRelations(List<GoTerm.ParentRelation> relations) {
        if (relations == null) {
            return List.of();
        }
        return relations.stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapParentRelationToCollection)
                .toList();
    }

    private static GoTermCollection.ParentRelation mapParentRelationToCollection(GoTerm.ParentRelation rel) {
        var relation = new GoTermCollection.ParentRelation();
        relation.setParent(rel.getParent());
        relation.setRelationship(rel.getRelationship());
        return relation;
    }

    private static GoTerm mapQuickGoTermToModel(GoTermResponse.GoTerm goTerm) {
        List<GoTerm.Synonym> synonyms = goTerm.synonyms() == null
                ? List.of()
                : goTerm.synonyms().stream()
                        .filter(Objects::nonNull)
                        .map(GoTermMapper::mapQuickGoSynonymToModel)
                        .toList();

        return GoTerm.builder()
                .termId(goTerm.id())
                .name(goTerm.name())
                .synonyms(synonyms)
                .build();
    }

    private static GoTerm.Synonym mapQuickGoSynonymToModel(GoTermResponse.GoTerm.Synonym syn) {
        return GoTerm.Synonym.builder()
                .name(syn.name())
                .type(syn.type())
                .build();
    }

    private static List<PathToRoot> mapPathGroup(List<PathToRootResponse> group) {
        return group.stream()
                .filter(Objects::nonNull)
                .map(GoTermMapper::mapPathToRoot)
                .toList();
    }

    private static PathToRoot mapPathToRoot(PathToRootResponse dto) {
        return PathToRoot.builder()
                .child(dto.child())
                .parent(dto.parent())
                .relationship(dto.relationship())
                .build();
    }
}
