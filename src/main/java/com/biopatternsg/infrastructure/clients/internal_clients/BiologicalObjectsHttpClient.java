package com.biopatternsg.infrastructure.clients.internal_clients;

import com.biopatternsg.infrastructure.dtos.UpdateMeshIdRequest;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "biological-objects-api")
public interface BiologicalObjectsHttpClient {

    @PATCH
    @Path("biological-object/update-mesh-id")
    Response buildMeshOntologyTree(@RequestBody UpdateMeshIdRequest updateMeshIdRequest);

}
