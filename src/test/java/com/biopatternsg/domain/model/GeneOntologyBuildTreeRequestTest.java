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
package com.biopatternsg.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneOntologyBuildTreeRequestTest {

    @Test
    void getGoTermIdsHandlesNullListsAndDeduplicatesSafely() {
        GeneOntologyBuildTreeRequest request = new GeneOntologyBuildTreeRequest(
                Arrays.asList("GO:0001", null),
                null,
                List.of("GO:0001", "GO:0002")
        );

        List<String> ids = request.getGoTermIds();

        assertEquals(List.of("GO:0001", "GO:0002"), ids);
        assertTrue(request.toString().contains("GO:0001"));
    }

    @Test
    void getGoTermIdsReturnsEmptyListWhenAllListsAreNull() {
        GeneOntologyBuildTreeRequest request = new GeneOntologyBuildTreeRequest(null, null, null);
        assertTrue(request.getGoTermIds().isEmpty());
    }
}
