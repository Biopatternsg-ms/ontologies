/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
