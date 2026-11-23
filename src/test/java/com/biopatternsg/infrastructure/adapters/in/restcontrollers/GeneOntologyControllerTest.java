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

import com.biopatternsg.domain.model.GeneOntologyBuildTreeRequest;
import com.biopatternsg.infrastructure.dtos.BuildGeneOntologyTreeRequest;
import com.biopatternsg.infrastructure.dtos.MessageResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeneOntologyControllerTest {

    @Test
    void buildTreeGOReturns202WithMessageResponseAndDispatchesMappedDomainModel() {
        AtomicReference<GeneOntologyBuildTreeRequest> captured = new AtomicReference<>();
        GeneOntologyController controller = new GeneOntologyController(captured::set, Runnable::run);

        BuildGeneOntologyTreeRequest request = new BuildGeneOntologyTreeRequest(
                List.of("GO:0005634"),
                List.of("GO:0003677"),
                List.of("GO:0006355")
        );

        Response response = controller.buildTreeGO(request);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
        assertEquals(new MessageResponse("Building Gene Ontology tree"), response.getEntity());
        assertNotNull(captured.get());
        assertEquals(3, captured.get().getGoTermIds().size());
    }
}
