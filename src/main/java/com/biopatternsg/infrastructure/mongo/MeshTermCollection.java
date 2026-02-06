package com.biopatternsg.infrastructure.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@MongoEntity(collection = "mesh_ontology")
public class MeshTermCollection extends PanacheMongoEntity {
    private String meshId;
    private String name;
    private String symbol;
    private List<String> synonyms;
    private List<String> parents;
}
