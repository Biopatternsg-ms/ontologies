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
package com.biopatternsg.infrastructure.external_services.impl;

import com.biopatternsg.domain.model.mesh.ESearchResult;
import com.biopatternsg.domain.model.mesh.ESummaryResult;
import com.biopatternsg.domain.model.mesh.SummaryMesh;
import com.biopatternsg.infrastructure.clients.external_clients.MeshOntologyHttpClient;
import com.biopatternsg.infrastructure.external_services.QueryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class QueryMeshImpl implements QueryMesh {

    private final MeshOntologyHttpClient meshHttpClient;

    public QueryMeshImpl(@RestClient MeshOntologyHttpClient meshHttpClient) {
        this.meshHttpClient = meshHttpClient;
    }

    //TODO: Agregar lib de reciliencia
    @Override
    public List<String> getTermIds(String biologicalObjectName) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ESearchResult result = meshHttpClient.search(
                "mesh",
                biologicalObjectName,
                10,
                "biomed3"
            );

        return result.getIdList().getIds();
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ESummaryResult result = meshHttpClient.summary("mesh", meshTermId);

        Set<String> allParents = result.getDocSum().getItems().stream()
                .filter(it -> "DS_IdxLinks".equals(it.getName()))
                .flatMap(it -> it.getValuesBySubItemName("Parent").stream())
                .collect(Collectors.toSet());

        Set<String> itemsIds = result.getDocSum().getItems().stream()
                .filter(it -> "DS_MeshTerms".equals(it.getName()))
                .flatMap(it -> it.getValuesBySubItemNameOtra("string").stream())
                .collect(Collectors.toSet());

        return SummaryMesh.builder()
                .parents(allParents)
                .meshSynonyms(itemsIds)
                .build();
    }




}
