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

import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.port.out.external_repositories.MeshOntologyRepoWeb;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class MeshOntologyServiceImpl implements MeshOntologyService {

    private final MeshOntologyRepoWeb meshOntologyRepoWeb;

    @Override
    public List<String> getMeshTermIds(String objectName) {
        return meshOntologyRepoWeb.getTermIds(objectName);
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        return meshOntologyRepoWeb.getSummarySynonyms(meshTermId);
    }
}