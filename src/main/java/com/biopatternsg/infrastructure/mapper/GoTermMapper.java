package com.biopatternsg.infrastructure.mapper;

import com.biopatternsg.infrastructure.mongo.GoTermCollection;
import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GoTermMapper {

    public List<GoTerm> toEntity(GoTermResponse response) {
        if (response == null || response.results() == null) {
            return List.of();
        }
        
        return response.results().stream()
                .map(this::mapGoTermToEntity)
                .collect(Collectors.toList());
    }

    public GoTerm toModel(GoTermCollection entity) {
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

    public GoTermCollection toEntity(GoTerm term) {
        if (term == null) {
            return null;
        }

        var entity = new GoTermCollection();
        entity.setTermId(term.getTermId());
        entity.setName(term.getName());
        entity.setSynonyms(mapModelToSynonyms(term.getSynonyms()));
        entity.setParentRelations(mapModelToParentRelations(term.getParentRelations()));

        return entity;
    }

    private List<GoTerm.Synonym> mapSynonymsToModel(List<GoTermCollection.Synonym> synonyms) {
        if (synonyms == null) {
            return null;
        }
        return synonyms.stream()
                .map(syn -> GoTerm.Synonym.builder()
                        .name(syn.getName())
                        .type(syn.getType())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GoTermCollection.Synonym> mapModelToSynonyms(List<GoTerm.Synonym> synonyms) {
        if (synonyms == null) {
            return null;
        }
        return synonyms.stream()
                .map(syn -> {
                    var synonym = new GoTermCollection.Synonym();
                    synonym.setName(syn.getName());
                    synonym.setType(syn.getType());
                    return synonym;
                })
                .collect(Collectors.toList());
    }

    private List<GoTerm.ParentRelation> mapParentRelationsToModel(List<GoTermCollection.ParentRelation> parentRelations) {
        if (parentRelations == null) {
            return null;
        }
        return parentRelations.stream()
                .map(rel -> GoTerm.ParentRelation.builder()
                        .parent(rel.getParent())
                        .relationship(rel.getRelationship())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GoTermCollection.ParentRelation> mapModelToParentRelations(List<GoTerm.ParentRelation> relations) {
        if (relations == null) {
            return null;
        }
        return relations.stream()
                .map(rel -> {
                    var relation = new GoTermCollection.ParentRelation();
                    relation.setParent(rel.getParent());
                    relation.setRelationship(rel.getRelationship());
                    return relation;
                })
                .collect(Collectors.toList());
    }

    private GoTerm mapGoTermToEntity(GoTermResponse.GoTerm goTerm) {
        if (goTerm == null) {
            return null;
        }

        var entity = new GoTerm();
        entity.setTermId(goTerm.id());
        entity.setName(goTerm.name());

        if (goTerm.synonyms() != null) {
            entity.setSynonyms(
                goTerm.synonyms().stream()
                    .map(syn -> {
                        var synEntity = new GoTerm.Synonym();
                        synEntity.setName(syn.name());
                        synEntity.setType(syn.type());
                        return synEntity;
                    })
                    .collect(Collectors.toList())
            );
        }

        return entity;
    }
}
