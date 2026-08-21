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

import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SearchMeshIdBySynonymsUseCaseTest {

    private StubMeshOntologyRepository stubRepository;
    private SearchMeshIdBySynonymsUseCase useCase;

    private static class StubMeshOntologyRepository implements MeshOntologyRepository {
        List<String> lastPassedSynonyms;
        MeshTermCollection returnTerm;

        @Override
        public void save(MeshTermCollection meshTermCollection) {}

        @Override
        public Optional<MeshTermCollection> findByMeshId(String meshId) {
            return Optional.empty();
        }

        @Override
        public Optional<MeshTermCollection> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public Optional<MeshTermCollection> findBySynonyms(List<String> synonyms) {
            return Optional.empty();
        }

        @Override
        public Optional<MeshTermCollection> findBySynonymsCaseInsensitive(List<String> synonyms) {
            this.lastPassedSynonyms = synonyms;
            return Optional.ofNullable(returnTerm);
        }
    }

    @BeforeEach
    void setUp() {
        stubRepository = new StubMeshOntologyRepository();
        useCase = new SearchMeshIdBySynonymsUseCase(stubRepository);
    }

    @Test
    void shouldReturnEmptyWhenSynonymsListIsNull() {
        Optional<String> result = useCase.execute(null);
        assertTrue(result.isEmpty());
        assertNull(stubRepository.lastPassedSynonyms);
    }

    @Test
    void shouldReturnEmptyWhenSynonymsListIsEmpty() {
        Optional<String> result = useCase.execute(Collections.emptyList());
        assertTrue(result.isEmpty());
        assertNull(stubRepository.lastPassedSynonyms);
    }

    @Test
    void shouldReturnEmptyWhenSynonymsListHasOnlyBlankStrings() {
        Optional<String> result = useCase.execute(List.of("   ", "", "  ,  "));
        assertTrue(result.isEmpty());
        assertNull(stubRepository.lastPassedSynonyms);
    }

    @Test
    void shouldReturnMeshIdWhenMatchIsFound() {
        MeshTermCollection term = new MeshTermCollection();
        term.setMeshId("D002784");
        term.setName("Cholesterol");
        stubRepository.returnTerm = term;

        Optional<String> result = useCase.execute(List.of("cholesterin", "cyp7a1"));

        assertTrue(result.isPresent());
        assertEquals("D002784", result.get());
        assertEquals(List.of("cholesterin", "cyp7a1"), stubRepository.lastPassedSynonyms);
    }

    @Test
    void shouldSplitCommaSeparatedSynonymsAndFindMatch() {
        MeshTermCollection term = new MeshTermCollection();
        term.setMeshId("D002784");
        term.setName("Cholesterol");
        stubRepository.returnTerm = term;

        Optional<String> result = useCase.execute(List.of("cholesterin, Cholest-5-en-3-ol"));

        assertTrue(result.isPresent());
        assertEquals("D002784", result.get());
        assertEquals(List.of("cholesterin", "Cholest-5-en-3-ol"), stubRepository.lastPassedSynonyms);
    }

    @Test
    void shouldReturnEmptyWhenNoMatchFoundInRepository() {
        stubRepository.returnTerm = null;

        Optional<String> result = useCase.execute(List.of("UnknownGene123"));

        assertTrue(result.isEmpty());
        assertEquals(List.of("UnknownGene123"), stubRepository.lastPassedSynonyms);
    }
}