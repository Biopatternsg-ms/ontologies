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

import com.biopatternsg.domain.model.mesh.MeshCategory;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CheckMeshTermTypeUseCaseTest {

    private StubMeshOntologyRepository stubRepository;
    private CheckMeshTermTypeUseCase useCase;

    private static class StubMeshOntologyRepository implements MeshOntologyRepository {
        Map<String, MeshInfo> store = new HashMap<>();

        @Override
        public void save(MeshInfo meshInfo) {
            if (meshInfo != null && meshInfo.getMeshId() != null) {
                store.put(meshInfo.getMeshId(), meshInfo);
            }
        }

        @Override
        public Optional<MeshInfo> findByMeshId(String meshId) {
            return Optional.ofNullable(store.get(meshId));
        }

        @Override
        public Optional<MeshInfo> findByName(String name) {
            return store.values().stream().filter(t -> Objects.equals(t.getName(), name)).findFirst();
        }

        @Override
        public Optional<MeshInfo> findBySynonyms(List<String> synonyms) {
            return Optional.empty();
        }

        @Override
        public Optional<MeshInfo> findBySynonymsCaseInsensitive(List<String> synonyms) {
            return Optional.empty();
        }
    }

    @BeforeEach
    void setUp() {
        stubRepository = new StubMeshOntologyRepository();
        useCase = new CheckMeshTermTypeUseCase(stubRepository);
    }

    private MeshInfo createTerm(String meshId, String name, List<String> synonyms, List<String> parents) {
        return MeshInfo.builder()
                .meshId(meshId)
                .name(name)
                .synonyms(synonyms != null ? synonyms : List.of())
                .parents(parents != null ? parents : List.of())
                .build();
    }

    @Test
    void shouldReturnFalseForNullOrBlankInputs() {
        assertFalse(useCase.execute(null, MeshCategory.PROTEIN));
        assertFalse(useCase.execute("", MeshCategory.PROTEIN));
        assertFalse(useCase.execute("D002784", null));
    }

    @Test
    void shouldReturnTrueWhenEnzymeAncestorIsFound() {
        // D002785 (Cholesterol 7-alpha-Hydroxylase) -> Parent: D004798 (Enzymes)
        stubRepository.save(createTerm("D002785", "Cholesterol 7-alpha-Hydroxylase", List.of("CYP7A1"), List.of("D004798")));
        stubRepository.save(createTerm("D004798", "Enzymes and Enzyme Coenzymes", List.of("Enzymes"), List.of("D000602")));
        stubRepository.save(createTerm("D000602", "Amino Acids, Peptides, and Proteins", List.of("Proteins"), List.of()));

        assertTrue(useCase.execute("D002785", MeshCategory.ENZYME));
        assertTrue(useCase.execute("D002785", MeshCategory.PROTEIN));
    }

    @Test
    void shouldReturnTrueWhenReceptorAncestorIsFound() {
        // D018160 (Receptors, Cytoplasmic and Nuclear) -> Parent: D011930 (Receptors, Cell Surface)
        stubRepository.save(createTerm("D018160", "Receptors, Cytoplasmic and Nuclear", List.of("Nuclear Receptors"), List.of("D011930")));
        stubRepository.save(createTerm("D011930", "Receptors, Cell Surface", List.of(), List.of("D008565")));

        assertTrue(useCase.execute("D018160", MeshCategory.RECEPTOR));
    }

    @Test
    void shouldReturnTrueWhenLigandAncestorIsFound() {
        // D007371 (Interferon-gamma) -> Parent: D036341 (Intercellular Signaling Peptides and Proteins)
        stubRepository.save(createTerm("D007371", "Interferon-gamma", List.of("IFN-gamma"), List.of("D036341")));
        stubRepository.save(createTerm("D036341", "Intercellular Signaling Peptides and Proteins", List.of(), List.of("D000602")));

        assertTrue(useCase.execute("D007371", MeshCategory.LIGAND));
    }

    @Test
    void shouldReturnTrueWhenTranscriptionFactorAncestorIsFound() {
        // D000074322 (Sterol Regulatory Element Binding Proteins) -> Parent: D014157 (Transcription Factors)
        stubRepository.save(createTerm("D000074322", "Sterol Regulatory Element Binding Protein 1", List.of("SREBP-1"), List.of("D014157")));
        stubRepository.save(createTerm("D014157", "Transcription Factors", List.of(), List.of("D000602")));

        assertTrue(useCase.execute("D000074322", MeshCategory.TRANSCRIPTION_FACTOR));
        assertTrue(useCase.execute("D000074322", MeshCategory.PROTEIN));
    }

    @Test
    void shouldReturnTrueWhenAdaptorProteinAncestorIsFound() {
        stubRepository.save(createTerm("D048868", "Adaptor Proteins, Signal Transducing", List.of("Adaptor Protein"), List.of()));
        stubRepository.save(createTerm("D00001", "GRB2", List.of(), List.of("D048868")));

        assertTrue(useCase.execute("D00001", MeshCategory.ADAPTOR_PROTEIN));
    }

    @Test
    void shouldHandleCyclesGracefullyWithoutInfiniteLoop() {
        // A -> B -> A
        stubRepository.save(createTerm("TERM_A", "Term A", List.of(), List.of("TERM_B")));
        stubRepository.save(createTerm("TERM_B", "Term B", List.of(), List.of("TERM_A")));

        assertFalse(useCase.execute("TERM_A", MeshCategory.PROTEIN));
    }

    @Test
    void shouldReturnFalseWhenNoMatchExistsInHierarchy() {
        // D002784 (Cholesterol) -> Parent: D013256 (Sterols) -> D008055 (Lipids)
        stubRepository.save(createTerm("D002784", "Cholesterol", List.of("Cholesterin"), List.of("D013256")));
        stubRepository.save(createTerm("D013256", "Sterols", List.of(), List.of("D008055")));
        stubRepository.save(createTerm("D008055", "Lipids", List.of(), List.of()));

        assertFalse(useCase.execute("D002784", MeshCategory.PROTEIN));
        assertFalse(useCase.execute("D002784", MeshCategory.ENZYME));
        assertFalse(useCase.execute("D002784", MeshCategory.LIGAND));
    }
}