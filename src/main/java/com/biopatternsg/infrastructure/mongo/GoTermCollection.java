package com.biopatternsg.infrastructure.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@MongoEntity(collection = "gene_ontology")
public class GoTermCollection extends PanacheMongoEntity {
    private String termId;
    private String name;
    private List<Synonym> synonyms = new ArrayList<>();
    private List<ParentRelation> parentRelations = new ArrayList<>();

    @Setter
    @Getter
    public static class Synonym {
        private String name;
        private String type;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentRelation {
        private String parent;
        private String relationship;
    }
}
