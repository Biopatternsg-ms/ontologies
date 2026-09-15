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

import com.biopatternsg.domain.model.mesh.MeshCategory;
import com.biopatternsg.domain.port.in.CheckMeshTermType;
import com.biopatternsg.domain.port.in.SearchMeshIdBySynonyms;
import com.biopatternsg.infrastructure.dtos.CheckMeshTypeRequest;
import com.biopatternsg.infrastructure.dtos.CheckMeshTypeResponse;
import com.biopatternsg.infrastructure.dtos.MeshIdResponse;
import com.biopatternsg.infrastructure.dtos.SearchMeshIdRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MeshOntologyControllerTest {

    @Test
    void shouldReturn200AndMeshIdWhenMatchFound() {
        SearchMeshIdBySynonyms searchPort = synonyms -> Optional.of("D002784");
        CheckMeshTermType checkTypePort = (meshId, type) -> true;
        MeshOntologyController controller = new MeshOntologyController(obj -> {}, searchPort, checkTypePort, Runnable::run);

        SearchMeshIdRequest request = new SearchMeshIdRequest(List.of("cholesterol", "cyp7a1"));
        Response response = controller.searchMeshId(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof MeshIdResponse);
        assertEquals("D002784", ((MeshIdResponse) response.getEntity()).meshId());
    }

    @Test
    void shouldReturn404WhenNoMatchFound() {
        SearchMeshIdBySynonyms searchPort = synonyms -> Optional.empty();
        CheckMeshTermType checkTypePort = (meshId, type) -> false;
        MeshOntologyController controller = new MeshOntologyController(obj -> {}, searchPort, checkTypePort, Runnable::run);

        SearchMeshIdRequest request = new SearchMeshIdRequest(List.of("unknown_term"));
        Response response = controller.searchMeshId(request);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"MeSH term not found for provided synonyms\"}", response.getEntity());
    }

    @Test
    void shouldReturn200WithIsTypeTrueWhenCheckTypeIsTrue() {
        SearchMeshIdBySynonyms searchPort = synonyms -> Optional.empty();
        CheckMeshTermType checkTypePort = (meshId, type) -> true;
        MeshOntologyController controller = new MeshOntologyController(obj -> {}, searchPort, checkTypePort, Runnable::run);

        CheckMeshTypeRequest request = new CheckMeshTypeRequest("D007371", MeshCategory.LIGAND);
        Response response = controller.checkType(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof CheckMeshTypeResponse);
        CheckMeshTypeResponse entity = (CheckMeshTypeResponse) response.getEntity();
        assertEquals("D007371", entity.meshId());
        assertEquals(MeshCategory.LIGAND, entity.type());
        assertTrue(entity.isType());
    }

    @Test
    void shouldReturn200WithIsTypeFalseWhenCheckTypeIsFalse() {
        SearchMeshIdBySynonyms searchPort = synonyms -> Optional.empty();
        CheckMeshTermType checkTypePort = (meshId, type) -> false;
        MeshOntologyController controller = new MeshOntologyController(obj -> {}, searchPort, checkTypePort, Runnable::run);

        CheckMeshTypeRequest request = new CheckMeshTypeRequest("D002784", MeshCategory.LIGAND);
        Response response = controller.checkType(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof CheckMeshTypeResponse);
        CheckMeshTypeResponse entity = (CheckMeshTypeResponse) response.getEntity();
        assertEquals("D002784", entity.meshId());
        assertEquals(MeshCategory.LIGAND, entity.type());
        assertFalse(entity.isType());
    }
}
