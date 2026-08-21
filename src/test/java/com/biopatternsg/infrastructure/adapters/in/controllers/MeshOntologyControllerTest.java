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

import com.biopatternsg.domain.port.in.SearchMeshIdBySynonyms;
import com.biopatternsg.domain.port.in.SendBiologicalObjectToQueue;
import com.biopatternsg.infrastructure.dtos.MeshIdResponse;
import com.biopatternsg.infrastructure.dtos.SearchMeshIdRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class MeshOntologyControllerTest {

    private StubSearchMeshIdBySynonyms stubSearchPort;
    private MeshOntologyController controller;

    private static class StubSearchMeshIdBySynonyms implements SearchMeshIdBySynonyms {
        List<String> lastPassedSynonyms;
        Optional<String> returnMeshId = Optional.empty();

        @Override
        public Optional<String> execute(List<String> synonyms) {
            this.lastPassedSynonyms = synonyms;
            return returnMeshId;
        }
    }

    @BeforeEach
    void setUp() {
        stubSearchPort = new StubSearchMeshIdBySynonyms();
        SendBiologicalObjectToQueue stubSendQueue = biologicalObject -> {};
        Executor directExecutor = Runnable::run;

        controller = new MeshOntologyController(stubSendQueue, stubSearchPort, directExecutor);
    }

    @Test
    void shouldReturn200AndMeshIdWhenMatchFound() {
        stubSearchPort.returnMeshId = Optional.of("D002784");

        SearchMeshIdRequest request = new SearchMeshIdRequest(List.of("cholesterol", "cyp7a1"));
        Response response = controller.searchMeshId(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof MeshIdResponse);
        assertEquals("D002784", ((MeshIdResponse) response.getEntity()).meshId());
        assertEquals(List.of("cholesterol", "cyp7a1"), stubSearchPort.lastPassedSynonyms);
    }

    @Test
    void shouldReturn404WhenNoMatchFound() {
        stubSearchPort.returnMeshId = Optional.empty();

        SearchMeshIdRequest request = new SearchMeshIdRequest(List.of("unknown_term"));
        Response response = controller.searchMeshId(request);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"MeSH term not found for provided synonyms\"}", response.getEntity());
        assertEquals(List.of("unknown_term"), stubSearchPort.lastPassedSynonyms);
    }
}