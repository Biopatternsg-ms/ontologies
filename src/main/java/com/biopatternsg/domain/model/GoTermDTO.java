package com.biopatternsg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoTermDTO {
    private String id;
    private String termId;
    private String name;
    private List<SynonymDTO> synonyms;
    private List<ParentRelationDTO> parentRelations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SynonymDTO {
        private String name;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentRelationDTO {
        private String parent;
        private String relationship;
    }
}
