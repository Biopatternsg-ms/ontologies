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

import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeshTermMapperTest {

    @Test
    void handlesNullInputsSafely() {
        assertNull(MeshTermMapper.collectionToModel(null));
        assertNull(MeshTermMapper.modelToCollection(null));
    }

    @Test
    void mapsCollectionAndModelWithNullListsNormalizedToEmpty() {
        MeshTermCollection entity = new MeshTermCollection();
        entity.setMeshId("D002784");
        entity.setName("Cholesterol");
        entity.setCategories(Map.of("PROTEIN", false));

        MeshInfo model = MeshTermMapper.collectionToModel(entity);
        assertNotNull(model);
        assertEquals("D002784", model.getMeshId());
        assertEquals("Cholesterol", model.getName());
        assertTrue(model.getSynonyms().isEmpty());
        assertTrue(model.getParents().isEmpty());

        MeshTermCollection roundTrip = MeshTermMapper.modelToCollection(
                MeshInfo.builder().meshId("D002784").name("Cholesterol").synonyms(List.of("cholesterin")).build()
        );
        assertEquals(List.of("cholesterin"), roundTrip.getSynonyms());
        assertTrue(roundTrip.getParents().isEmpty());
    }
}
