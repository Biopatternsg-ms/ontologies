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

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.out.producers.MeshQueueSender;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SendBiologicalObjectToQueueUseCaseTest {

    @Test
    void executeResolvesSymbolWithoutMutatingBiologicalObjectAndDispatches() {
        var repo = new InMemoryMeshRepo(Optional.empty());
        var sender = new RecordingQueueSender();
        var useCase = new SendBiologicalObjectToQueueUseCase(repo, sender);

        BiologicalObject obj = BiologicalObject.builder()
                .id("1")
                .name("   ")
                .symbol("  TP53  ")
                .synonyms(List.of("p53"))
                .build();

        useCase.execute(obj);

        assertEquals("   ", obj.getName());
        assertEquals("TP53", repo.lastQueriedName);
        assertEquals(obj, sender.dispatched);
    }

    @Test
    void executeSkipsDispatchWhenResolvedNameAlreadyExists() {
        var repo = new InMemoryMeshRepo(Optional.of(MeshInfo.builder().meshId("D016159").name("TP53").build()));
        var sender = new RecordingQueueSender();
        var useCase = new SendBiologicalObjectToQueueUseCase(repo, sender);

        BiologicalObject obj = BiologicalObject.builder().id("1").symbol("TP53").build();
        useCase.execute(obj);

        assertNull(sender.dispatched);
    }

    private static class RecordingQueueSender implements MeshQueueSender {
        BiologicalObject dispatched;

        @Override
        public void senderBiologicalObject(BiologicalObject biologicalObject) {
            this.dispatched = biologicalObject;
        }
    }

    private static class InMemoryMeshRepo implements MeshOntologyRepository {
        private final Optional<MeshInfo> byNameResult;
        String lastQueriedName;

        InMemoryMeshRepo(Optional<MeshInfo> byNameResult) {
            this.byNameResult = byNameResult;
        }

        @Override
        public void save(MeshInfo meshInfo) {}

        @Override
        public Optional<MeshInfo> findByMeshId(String meshId) { return Optional.empty(); }

        @Override
        public Optional<MeshInfo> findByName(String name) {
            this.lastQueriedName = name;
            return byNameResult;
        }

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
