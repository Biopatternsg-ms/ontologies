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
package com.biopatternsg.infrastructure.adapters.in.restcontrollers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.in.CheckMeshTermType;
import com.biopatternsg.domain.port.in.SearchMeshIdBySynonyms;
import com.biopatternsg.domain.port.in.SendBiologicalObjectToQueue;
import com.biopatternsg.infrastructure.dtos.CheckMeshTypeRequest;
import com.biopatternsg.infrastructure.dtos.CheckMeshTypeResponse;
import com.biopatternsg.infrastructure.dtos.MeshIdResponse;
import com.biopatternsg.infrastructure.dtos.SearchMeshIdRequest;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
@Path("/mesh-ontology")
public class MeshOntologyController {

    private final SendBiologicalObjectToQueue sendBiologicalObjectToQueue;
    private final SearchMeshIdBySynonyms searchMeshIdBySynonyms;
    private final CheckMeshTermType checkMeshTermType;
    private final Executor executor;

    @POST
    @Path("/check-type")
    @Operation(
            summary = "Check MeSH node type",
            description = "Evaluates whether a MeSH node or its ancestors in the ontology hierarchy match a specified category (e.g. PROTEIN, ENZYME, RECEPTOR, LIGAND, etc.)."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Type evaluation result",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CheckMeshTypeResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "Error message"
                            )
                    )
            )
    })
    public Response checkType(@Valid CheckMeshTypeRequest request) {
        boolean isType = checkMeshTermType.execute(request.meshId(), request.type());
        CheckMeshTypeResponse response = new CheckMeshTypeResponse(request.meshId(), request.type(), isType);
        return Response.ok(response).build();
    }

    @POST
    @Path("/search-mesh-id")
    @Operation(
            summary = "Search MeSH ID by synonyms",
            description = "Searches for a matching MeSH term by checking synonyms against name and synonyms in mesh_ontology case-insensitively."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "MeSH ID found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MeshIdResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "MeSH term not found for provided synonyms",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "Not found error message"
                            )
                    )
            )
    })
    public Response searchMeshId(@Valid SearchMeshIdRequest request) {
        Optional<String> meshIdOptional = searchMeshIdBySynonyms.execute(request.synonyms());

        return meshIdOptional
                .map(meshId -> Response.ok(new MeshIdResponse(meshId)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"MeSH term not found for provided synonyms\"}")
                        .build());
    }

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
