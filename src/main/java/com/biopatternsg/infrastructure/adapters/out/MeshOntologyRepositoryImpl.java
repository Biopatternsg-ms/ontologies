package com.biopatternsg.infrastructure.adapters.out;

import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MeshOntologyRepositoryImpl implements MeshOntologyRepository, PanacheMongoRepository<MeshTermCollection> {

    @Override
    public void save(MeshTermCollection meshTermCollection) {
        persistOrUpdate(meshTermCollection);
    }

    @Override
    public Optional<MeshTermCollection> findByMeshId(String meshId) {
        return find("{meshId: ?1}", meshId).firstResultOptional();
    }

    @Override
    public Optional<MeshTermCollection> findByName(String name) {
        return find("{name: ?1}", name).firstResultOptional();
    }

    @Override
    public Optional<MeshTermCollection> findBySynonyms(List<String> synonyms) {
        return find("{$or: [{synonyms: {$in: ?1}}, {name: {$in: ?1}}]}", synonyms).firstResultOptional();
    }
}
