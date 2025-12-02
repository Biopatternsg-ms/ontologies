package com.biopatternsg.infrastructure.clients.external_clients;

import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ebi-api")
@Path("/")
public interface GeneOntologyHttpClient {

    @GET
    @Path("/{goId}/paths/GO:0008150;GO:0003674;GO:0005575")
    PathsToRoot getPathsToRoot(@PathParam("goId") String goId);

    @GET
    @Path("/{goIdList}")
    GoTermResponse findGeneOntologies(@PathParam("goIdList") String goIdList);

}
