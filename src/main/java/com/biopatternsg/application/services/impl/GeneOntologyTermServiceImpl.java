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
package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.GeneOntologyTermService;
import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.mapper.GoTermMapper;
import com.biopatternsg.domain.port.out.external_repositories.GeneOntologyTermRepoWeb;
import com.biopatternsg.domain.port.out.repositories.GeneOntologyTermRepository;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class GeneOntologyTermServiceImpl implements GeneOntologyTermService {

    private final GeneOntologyTermRepoWeb goOntologyRepoWeb;
    private final GeneOntologyTermRepository goTermRepository;
    private final GoTermMapper goTermMapper;

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return goOntologyRepoWeb.getPathsToRoot(goId);
    }

    @Override
    public List<GoTerm> getByIdsInWeb(List<String> ids) {
        GoTermResponse response = goOntologyRepoWeb.findGOTerms(ids);
        return goTermMapper.toEntity(response);
    }

    @Override
    public List<GoTerm> getByIdsInDB(List<String> ids) {
        return goTermRepository.findByIds(ids).stream().map(goTermMapper::toModel).toList();
    }

}
