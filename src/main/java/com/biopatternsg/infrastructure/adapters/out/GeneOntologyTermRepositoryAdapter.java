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

import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import com.biopatternsg.infrastructure.mapper.GoTermMapper;
import com.biopatternsg.infrastructure.mongo.GoTermCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class GeneOntologyTermRepositoryAdapter implements GeneOntologyTermRepository, PanacheMongoRepository<GoTermCollection> {

    private final GoTermMapper goTermMapper;

    @Override
    public List<GoTerm> findByIds(List<String> ids) {
        return find("{termId: {$in: ?1}}", ids).list().stream()
                .map(goTermMapper::toModel)
                .toList();
    }

    @Override
    public void save(GoTerm goTerm) {
        persistOrUpdate(goTermMapper.toEntity(goTerm));
    }
}
