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
package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.BiologicalObjectsService;
import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.model.mesh.SummaryMesh;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuildMeshOntologyTreeUseCaseTest {

    @Test
    void executeSkipsExternalLookupWhenNameAlreadyExistsAndDoesNotMutateInput() {
        var meshService = new SpyMeshOntologyService();
        var repo = new StubMeshRepo(Optional.of(MeshInfo.builder().meshId("D001").name("BRCA1").build()));
        BiologicalObjectsService bioService = (id, meshId) -> {};
        var useCase = new BuildMeshOntologyTreeUseCase(meshService, repo, bioService);

        BiologicalObject obj = BiologicalObject.builder()
                .id("10")
                .name(null)
                .symbol("  BRCA1 ")
                .build();

        useCase.execute(obj);

        assertNull(obj.getName());
        assertEquals(0, meshService.getMeshTermIdsCalls);
        assertEquals(0, meshService.getSummarySynonymsCalls);
    }

    private static class SpyMeshOntologyService implements MeshOntologyService {
        int getMeshTermIdsCalls = 0;
        int getSummarySynonymsCalls = 0;

        @Override
        public List<String> getMeshTermIds(String objectName) {
            getMeshTermIdsCalls++;
            return List.of();
        }

        @Override
        public SummaryMesh getSummarySynonyms(String meshTermId) {
            getSummarySynonymsCalls++;
            return SummaryMesh.builder().parents(Set.of()).meshSynonyms(Set.of()).build();
        }
    }

    private static class StubMeshRepo implements MeshOntologyRepository {
        private final Optional<MeshInfo> byName;

        StubMeshRepo(Optional<MeshInfo> byName) {
            this.byName = byName;
        }

        @Override
        public void save(MeshInfo meshInfo) {}

        @Override
        public Optional<MeshInfo> findByMeshId(String meshId) { return Optional.empty(); }

        @Override
        public Optional<MeshInfo> findByName(String name) { return byName; }

        @Override
        public Optional<MeshInfo> findBySynonyms(List<String> synonyms) { return Optional.empty(); }

        @Override
        public Optional<MeshInfo> findBySynonymsCaseInsensitive(List<String> synonyms) { return Optional.empty(); }

        @Override
        public void updateCategories(String meshId, Map<String, Boolean> categories) {}

        @Override
        public void updateCategory(String meshId, String category, boolean isType) {}
    }
}
