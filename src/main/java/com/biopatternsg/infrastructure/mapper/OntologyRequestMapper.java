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

import java.util.List;

public final class OntologyRequestMapper {

    private OntologyRequestMapper() {
    }

    public static GeneOntologyBuildTreeRequest requestToModel(BuildGeneOntologyTreeRequest request) {
        if (request == null) {
            return null;
        }
        return new GeneOntologyBuildTreeRequest(
                safeList(request.cellularComponent()),
                safeList(request.molecularFunction()),
                safeList(request.biologicalProcess())
        );
    }

    public static BiologicalObject requestToModel(BuildMeshTreeRequest request) {
        if (request == null) {
            return null;
        }
        return BiologicalObject.builder()
                .id(request.id())
                .name(request.name())
                .symbol(request.symbol())
                .synonyms(safeList(request.synonyms()))
                .build();
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
