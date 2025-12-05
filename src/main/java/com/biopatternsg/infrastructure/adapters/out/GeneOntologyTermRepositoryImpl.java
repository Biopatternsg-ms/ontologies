package com.biopatternsg.infrastructure.adapters.out;

import com.biopatternsg.infrastructure.mongo.GoTermCollection;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GeneOntologyTermRepositoryImpl implements GeneOntologyTermRepository, PanacheMongoRepository<GoTermCollection> {

    @Override
    public List<GoTermCollection> findByIds(List<String> ids) {
        return find("{termId: {$in: ?1}}", ids).list();
    }

    @Override
    public void save(GoTermCollection GoTerm) {
        persistOrUpdate(GoTerm);
    }
}
