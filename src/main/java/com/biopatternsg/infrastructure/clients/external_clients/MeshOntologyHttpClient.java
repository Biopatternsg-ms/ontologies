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
