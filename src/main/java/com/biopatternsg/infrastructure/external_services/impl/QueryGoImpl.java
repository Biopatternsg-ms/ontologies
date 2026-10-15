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

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.clients.external_clients.GeneOntologyHttpClient;
import com.biopatternsg.infrastructure.external_services.QueryGO;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Collections;

@Slf4j
@ApplicationScoped
public class QueryGoImpl implements QueryGO {

    @RestClient
    @Inject
    private final GeneOntologyHttpClient goHttpClient;

    public QueryGoImpl(@RestClient GeneOntologyHttpClient goHttpClient) {
        this.goHttpClient = goHttpClient;
    }

    @Override
    public PathsToRoot getPathsToRoot(String goId) {
        if (goId == null || goId.isBlank()) {
            return PathsToRoot.builder().numberOfHits(0).results(Collections.emptyList()).build();
        }

        try {
            return goHttpClient.getPathsToRoot(goId);
        } catch (WebApplicationException | ProcessingException e) {
            log.warn("Error querying QuickGO paths to root for GO term {}: {}", goId, e.getMessage());
            return PathsToRoot.builder().numberOfHits(0).results(Collections.emptyList()).build();
        } catch (Exception e) {
            log.error("Unexpected error querying QuickGO paths to root for GO term {}: {}", goId, e.getMessage(), e);
            return PathsToRoot.builder().numberOfHits(0).results(Collections.emptyList()).build();
        }
    }

    @Override
    public GoTermResponse findGeneOntologies(String joinIds) {
        return goHttpClient.findGeneOntologies(joinIds);
    }
}
