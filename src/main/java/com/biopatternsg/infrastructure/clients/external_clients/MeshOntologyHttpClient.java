package com.biopatternsg.infrastructure.clients.external_clients;

import com.biopatternsg.domain.model.mesh.ESearchResult;
import com.biopatternsg.domain.model.mesh.ESummaryResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

@RegisterRestClient(configKey = "ncbi-api")
@RegisterProvider(DoctypeRemovalFilter.class)
public interface MeshOntologyHttpClient {

    @GET
    @Path("/esearch.fcgi")
    @Produces({MediaType.APPLICATION_XML, "text/xml"})
    ESearchResult search(
        @QueryParam("db") String db,
        @QueryParam("term") String term,
        @QueryParam("retmax") int retmax,
        @QueryParam("tool") String tool
    );

    @GET
    @Path("esummary.fcgi")
    @Produces({MediaType.APPLICATION_XML, "text/xml"})
    ESummaryResult summary(
        @QueryParam("db") String db,
        @QueryParam("id") String id
    );
}
