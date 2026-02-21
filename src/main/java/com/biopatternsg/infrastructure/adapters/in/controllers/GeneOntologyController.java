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
