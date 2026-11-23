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
package com.biopatternsg.infrastructure.mapper;

import com.biopatternsg.domain.model.GeneOntologyBuildTreeRequest;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.infrastructure.dtos.BuildGeneOntologyTreeRequest;
import com.biopatternsg.infrastructure.dtos.BuildMeshTreeRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OntologyRequestMapperTest {

    @Test
    void requestToModelHandlesNullInputs() {
        assertNull(OntologyRequestMapper.requestToModel((BuildGeneOntologyTreeRequest) null));
        assertNull(OntologyRequestMapper.requestToModel((BuildMeshTreeRequest) null));
    }

    @Test
    void requestToModelMapsGeneOntologyRequestSafelyWithNullLists() {
        BuildGeneOntologyTreeRequest dto = new BuildGeneOntologyTreeRequest(
                List.of("GO:0001"), null, List.of("GO:0002")
        );

        GeneOntologyBuildTreeRequest model = OntologyRequestMapper.requestToModel(dto);

        assertNotNull(model);
        assertEquals(List.of("GO:0001"), model.getCellularComponent());
        assertTrue(model.getMolecularFunction().isEmpty());
        assertEquals(List.of("GO:0002"), model.getBiologicalProcess());
        assertEquals(2, model.getGoTermIds().size());
    }

    @Test
    void requestToModelMapsMeshRequest() {
        BuildMeshTreeRequest dto = new BuildMeshTreeRequest("1", "TP53", "TP53", List.of("p53"));

        BiologicalObject model = OntologyRequestMapper.requestToModel(dto);

        assertNotNull(model);
        assertEquals("1", model.getId());
        assertEquals("TP53", model.getName());
        assertEquals("TP53", model.getSymbol());
        assertEquals(List.of("p53"), model.getSynonyms());
    }
}
