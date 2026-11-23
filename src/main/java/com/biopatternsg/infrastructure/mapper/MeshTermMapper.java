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

import java.util.List;

public final class MeshTermMapper {

    private MeshTermMapper() {
    }

    public static MeshInfo collectionToModel(MeshTermCollection entity) {
        if (entity == null) {
            return null;
        }
        return MeshInfo.builder()
                .meshId(entity.getMeshId())
                .name(entity.getName())
                .synonyms(safeList(entity.getSynonyms()))
                .parents(safeList(entity.getParents()))
                .categories(entity.getCategories())
                .build();
    }

    public static MeshTermCollection modelToCollection(MeshInfo model) {
        if (model == null) {
            return null;
        }
        MeshTermCollection entity = new MeshTermCollection();
        entity.setMeshId(model.getMeshId());
        entity.setName(model.getName());
        entity.setSynonyms(safeList(model.getSynonyms()));
        entity.setParents(safeList(model.getParents()));
        entity.setCategories(model.getCategories());
        return entity;
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
