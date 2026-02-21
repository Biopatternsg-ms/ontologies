package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.infrastructure.mongo.MeshTermCollection;

import java.util.List;
import java.util.Optional;

public interface MeshOntologyRepository {
    void save(MeshTermCollection meshTermCollection);
    Optional<MeshTermCollection> findByMeshId(String meshId);
    Optional<MeshTermCollection> findByName(String name);
    Optional<MeshTermCollection> findBySynonyms(List<String> synonyms);
}
