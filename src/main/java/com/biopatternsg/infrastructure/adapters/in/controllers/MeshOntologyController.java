package com.biopatternsg.infrastructure.adapters.in.controllers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.in.SendBiologicalObjectToQueue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
@Path("/mesh-ontology")
public class MeshOntologyController {

    private final SendBiologicalObjectToQueue sendBiologicalObjectToQueue;
    private final Executor executor;

    @POST
    @Path("/mesh")
    public Response buildTreeMesh(@Valid BiologicalObject biologicalObjects) {

        CompletableFuture.runAsync(() -> {
            try {
                sendBiologicalObjectToQueue.execute(biologicalObjects);
            } catch (Exception e) {
                log.error("Error building mesh ontology tree", e);
            }
        }, executor);

        return Response.accepted()
                .entity("{\"message\": \"Buildind mesh tree\"}")
                .build();
    }

}
