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
package com.biopatternsg.infrastructure.adapters.out.external_repositories;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.domain.port.out.external_repositories.GeneOntologyTermRepoWeb;
import com.biopatternsg.infrastructure.external_services.QueryGO;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GOAdapter implements GeneOntologyTermRepoWeb {

    private final QueryGO queryGO;

    public GOAdapter(QueryGO queryGO) {
        this.queryGO = queryGO;
    }

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        return queryGO.getPathsToRoot(goId);
    }

    @Override
    public GoTermResponse findGOTerms(List<String> goTermIds) {
        String joinIds = String.join(",", goTermIds);
        return queryGO.findGeneOntologies(joinIds);
    }
}
