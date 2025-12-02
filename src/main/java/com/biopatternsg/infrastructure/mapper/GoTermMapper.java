package com.biopatternsg.infrastructure.mapper;

import com.biopatternsg.infrastructure.mongo.GoTermCollection;
import com.biopatternsg.domain.model.GoTermDTO;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GoTermMapper {

    public List<GoTermDTO> toEntity(GoTermResponse response) {
        if (response == null || response.results() == null) {
            return List.of();
        }
        
        return response.results().stream()
                .map(this::mapGoTermToEntity)
                .collect(Collectors.toList());
    }

    public GoTermDTO toDTO(GoTermCollection entity) {
        if (entity == null) {
            return null;
        }

        return GoTermDTO.builder()
                .id(entity.id != null ? entity.id.toString() : null)
                .termId(entity.getTermId())
                .name(entity.getName())
                .synonyms(mapSynonymsToDTO(entity.getSynonyms()))
                .parentRelations(mapParentRelationsToDTO(entity.getParentRelations()))
                .build();
    }

    public GoTermCollection toEntity(GoTermDTO dto) {
        if (dto == null) {
            return null;
        }

        var entity = new GoTermCollection();
        entity.setTermId(dto.getTermId());
        entity.setName(dto.getName());
        entity.setSynonyms(mapDTOToSynonyms(dto.getSynonyms()));
        entity.setParentRelations(mapDTOToParentRelations(dto.getParentRelations()));

        return entity;
    }

    private List<GoTermDTO.SynonymDTO> mapSynonymsToDTO(List<GoTermCollection.Synonym> synonyms) {
        if (synonyms == null) {
            return null;
        }
        return synonyms.stream()
                .map(syn -> GoTermDTO.SynonymDTO.builder()
                        .name(syn.getName())
                        .type(syn.getType())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GoTermCollection.Synonym> mapDTOToSynonyms(List<GoTermDTO.SynonymDTO> synonymDTOs) {
        if (synonymDTOs == null) {
            return null;
        }
        return synonymDTOs.stream()
                .map(dto -> {
                    var synonym = new GoTermCollection.Synonym();
                    synonym.setName(dto.getName());
                    synonym.setType(dto.getType());
                    return synonym;
                })
                .collect(Collectors.toList());
    }

    private List<GoTermDTO.ParentRelationDTO> mapParentRelationsToDTO(List<GoTermCollection.ParentRelation> parentRelations) {
        if (parentRelations == null) {
            return null;
        }
        return parentRelations.stream()
                .map(rel -> GoTermDTO.ParentRelationDTO.builder()
                        .parent(rel.getParent())
                        .relationship(rel.getRelationship())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GoTermCollection.ParentRelation> mapDTOToParentRelations(List<GoTermDTO.ParentRelationDTO> relationDTOs) {
        if (relationDTOs == null) {
            return null;
        }
        return relationDTOs.stream()
                .map(dto -> {
                    var relation = new GoTermCollection.ParentRelation();
                    relation.setParent(dto.getParent());
                    relation.setRelationship(dto.getRelationship());
                    return relation;
                })
                .collect(Collectors.toList());
    }

    private GoTermDTO mapGoTermToEntity(GoTermResponse.GoTerm goTerm) {
        if (goTerm == null) {
            return null;
        }

        var entity = new GoTermDTO();
        entity.setTermId(goTerm.id());
        entity.setName(goTerm.name());

        if (goTerm.synonyms() != null) {
            entity.setSynonyms(
                goTerm.synonyms().stream()
                    .map(syn -> {
                        var synEntity = new GoTermDTO.SynonymDTO();
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
