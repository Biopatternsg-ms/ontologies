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
package com.biopatternsg.infrastructure.internal_services.impl;

import com.biopatternsg.infrastructure.clients.internal_clients.BiologicalObjectsHttpClient;
import com.biopatternsg.infrastructure.dtos.UpdateMeshIdRequest;
import com.biopatternsg.infrastructure.internal_services.QueryBiologicalObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class QueryBiologicalObjectsImpl implements QueryBiologicalObjects {

    private final BiologicalObjectsHttpClient biologicalObjectsHttpClient;

    public QueryBiologicalObjectsImpl(@RestClient BiologicalObjectsHttpClient biologicalObjectsHttpClient) {
        this.biologicalObjectsHttpClient = biologicalObjectsHttpClient;
    }

    @Override
    public Response updateMeshId(String biologicalObjectId, String meshId) {
        try {
            return biologicalObjectsHttpClient.buildMeshOntologyTree(new UpdateMeshIdRequest(biologicalObjectId, meshId));
        } catch (Exception e) {
            log.error("Error updating mesh MeshId for biological object {}: {}", biologicalObjectId, e.getMessage());
            return Response.serverError().build();
        }
    }
}
