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
