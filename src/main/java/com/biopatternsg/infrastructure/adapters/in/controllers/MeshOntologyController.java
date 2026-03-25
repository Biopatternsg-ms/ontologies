package com.biopatternsg.infrastructure.adapters.in.controllers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.in.SendBiologicalObjectToQueue;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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
    @Operation(
            summary = "Send biological object to queue",
            description = "Processes and sends biological object data to the message queue for further processing."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "202",
                    description = "Biological object successfully queued for processing",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "Success message"
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error occurred while building mesh ontology tree",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "Error message"
                            )
                    )
            )
    })
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
