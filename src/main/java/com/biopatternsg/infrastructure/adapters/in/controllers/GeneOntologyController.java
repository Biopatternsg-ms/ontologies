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
package com.biopatternsg.infrastructure.adapters.in.controllers;

import com.biopatternsg.domain.model.GeneOntologyBuildTreeRequest;
import com.biopatternsg.domain.port.in.BuildGeneOntologyTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
@Path("/gene-ontology")
public class GeneOntologyController {

    private final BuildGeneOntologyTree buildGeneOntologyTree;
    private final Executor executor;

    @POST
    @Path("/build-tree")
    @Operation(
            summary = "Build Gene Ontology tree",
            description = "Initiates the process of building a Gene Ontology tree based on the provided request parameters."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "202",
                    description = "Gene Ontology tree building process started successfully",
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
                    description = "Internal server error occurred while building gene ontology tree",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    description = "Error message"
                            )
                    )
            )
    })
    public Response buildTreeGO(@Valid GeneOntologyBuildTreeRequest geneOntologyBuildTreeRequest) {
        CompletableFuture.runAsync(() -> {
            try {
                buildGeneOntologyTree.execute(geneOntologyBuildTreeRequest);
            } catch (Exception e) {
                log.error("Error building gene ontology tree", e);
            }
        }, executor);

        return Response.accepted()
                .entity("{\"message\": \"Building Gene Ontology tree\"}")
                .build();
    }
}
