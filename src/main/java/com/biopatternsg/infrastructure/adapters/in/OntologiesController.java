package com.biopatternsg.infrastructure.adapters.in;

import com.biopatternsg.domain.model.*;
import com.biopatternsg.domain.port.in.BuildGeneOntologyTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
@Path("/gene-ontology")
public class OntologiesController {

    private final BuildGeneOntologyTree buildGeneOntologyTree;
    @Inject
    Executor executor;

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
