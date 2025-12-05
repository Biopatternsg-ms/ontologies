package com.biopatternsg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoTerm {
    private String id;
    private String termId;
    private String name;
    private List<Synonym> synonyms;
    private List<ParentRelation> parentRelations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Synonym {
        private String name;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentRelation {
        private String parent;
        private String relationship;
    }
}
