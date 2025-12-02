package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.infrastructure.mongo.GoTermCollection;

import java.util.List;

public interface GeneOntologyTermRepository {
    List<GoTermCollection> findByIds(List<String> ids);
    void save(GoTermCollection goTermCollection);
}
