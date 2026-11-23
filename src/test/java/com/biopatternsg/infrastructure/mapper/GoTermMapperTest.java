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

import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.model.PathsToRoot;
import com.biopatternsg.infrastructure.external_services.dto.go.GoTermResponse;
import com.biopatternsg.infrastructure.external_services.dto.go.PathToRootResponse;
import com.biopatternsg.infrastructure.external_services.dto.go.PathsToRootResponse;
import com.biopatternsg.infrastructure.mongo.GoTermCollection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoTermMapperTest {

    @Test
    void pathsToRootToModelHandlesNullResponseAndNullResults() {
        PathsToRoot fromNull = GoTermMapper.pathsToRootToModel(null);
        assertNotNull(fromNull);
        assertEquals(0, fromNull.getNumberOfHits());
        assertTrue(fromNull.getResults().isEmpty());

        PathsToRoot fromNullResults = GoTermMapper.pathsToRootToModel(new PathsToRootResponse(2, null));
        assertEquals(2, fromNullResults.getNumberOfHits());
        assertTrue(fromNullResults.getResults().isEmpty());
    }

    @Test
    void pathsToRootToModelMapsNestedDtoRecordsToDomainModels() {
        PathsToRootResponse dto = new PathsToRootResponse(
                1,
                List.of(List.of(new PathToRootResponse("GO:0001", "GO:0008150", "is_a")))
        );

        PathsToRoot domain = GoTermMapper.pathsToRootToModel(dto);

        assertEquals(1, domain.getNumberOfHits());
        assertEquals(1, domain.getResults().size());
        assertEquals("GO:0001", domain.getResults().getFirst().getFirst().getChild());
        assertEquals("GO:0008150", domain.getResults().getFirst().getFirst().getParent());
        assertEquals("is_a", domain.getResults().getFirst().getFirst().getRelationship());
    }

    @Test
    void collectionAndQuickGoMappingsHandleNullsSafely() {
        assertNull(GoTermMapper.collectionToModel(null));
        assertNull(GoTermMapper.modelToCollection(null));
        assertTrue(GoTermMapper.quickGoToModelList(null).isEmpty());
        assertTrue(GoTermMapper.quickGoToModelList(new GoTermResponse(0, null, null)).isEmpty());

        GoTermCollection collection = new GoTermCollection();
        collection.setTermId("GO:0005634");
        collection.setName("nucleus");

        GoTerm model = GoTermMapper.collectionToModel(collection);
        assertNotNull(model);
        assertTrue(model.getSynonyms().isEmpty());
        assertTrue(model.getParentRelations().isEmpty());
    }
}
