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

import com.biopatternsg.domain.port.out.external_repositories.MeshOntologyRepoWeb;
import com.biopatternsg.infrastructure.external_services.QueryMesh;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@ApplicationScoped
public class MeshAdapter implements MeshOntologyRepoWeb {

    private final QueryMesh queryMesh;

    @Override
    public List<String> getTermIds(String biologicalObjectName) {
        return queryMesh.getTermIds(biologicalObjectName);
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        return queryMesh.getSummarySynonyms(meshTermId);
    }
}
