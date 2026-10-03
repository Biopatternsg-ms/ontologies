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
        int updateCategoriesCount = 0;
        int updateCategoryCount = 0;

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

        @Override
        public void updateCategories(String meshId, Map<String, Boolean> categories) {
            updateCategoriesCount++;
            MeshInfo info = store.get(meshId);
            if (info != null) {
                if (info.getCategories() == null) {
                    info.setCategories(new HashMap<>());
                }
                info.getCategories().putAll(categories);
            }
        }

        @Override
        public void updateCategory(String meshId, String category, boolean isType) {
            updateCategoryCount++;
            MeshInfo info = store.get(meshId);
            if (info != null) {
                if (info.getCategories() == null) {
                    info.setCategories(new HashMap<>());
                }
                info.getCategories().put(category, isType);
            }
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

    @Test
    void shouldUpdateCategoryWhenExecuteIsCalled() {
        stubRepository.save(createTerm("D002785", "Cholesterol 7-alpha-Hydroxylase", List.of("CYP7A1"), List.of("D004798")));
        stubRepository.save(createTerm("D004798", "Enzymes and Enzyme Coenzymes", List.of("Enzymes"), List.of("D000602")));
        stubRepository.save(createTerm("D000602", "Amino Acids, Peptides, and Proteins", List.of("Proteins"), List.of()));

        assertTrue(useCase.execute("D002785", MeshCategory.ENZYME));

        MeshInfo updated = stubRepository.findByMeshId("D002785").orElseThrow();
        assertNotNull(updated.getCategories());
        assertTrue(updated.getCategories().get(MeshCategory.ENZYME.name()));
    }

    @Test
    void shouldExecuteAllAndSaveAllCategories() {
        stubRepository.save(createTerm("D002785", "Cholesterol 7-alpha-Hydroxylase", List.of("CYP7A1"), List.of("D004798")));
        stubRepository.save(createTerm("D004798", "Enzymes and Enzyme Coenzymes", List.of("Enzymes"), List.of("D000602")));
        stubRepository.save(createTerm("D000602", "Amino Acids, Peptides, and Proteins", List.of("Proteins"), List.of()));

        Map<MeshCategory, Boolean> results = useCase.executeAll("D002785");

        assertEquals(MeshCategory.values().length, results.size());
        assertTrue(results.get(MeshCategory.ENZYME));
        assertTrue(results.get(MeshCategory.PROTEIN));
        assertFalse(results.get(MeshCategory.RECEPTOR));
        assertFalse(results.get(MeshCategory.LIGAND));
        assertFalse(results.get(MeshCategory.TRANSCRIPTION_FACTOR));
        assertFalse(results.get(MeshCategory.ADAPTOR_PROTEIN));

        MeshInfo saved = stubRepository.findByMeshId("D002785").orElseThrow();
        assertNotNull(saved.getCategories());
        assertEquals(MeshCategory.values().length, saved.getCategories().size());
        assertTrue(saved.getCategories().get("ENZYME"));
        assertTrue(saved.getCategories().get("PROTEIN"));
        assertFalse(saved.getCategories().get("RECEPTOR"));
        assertFalse(saved.getCategories().get("LIGAND"));
        assertFalse(saved.getCategories().get("TRANSCRIPTION_FACTOR"));
        assertFalse(saved.getCategories().get("ADAPTOR_PROTEIN"));

        // Verify single atomic write: exactly 1 batch update, 0 individual category updates
        assertEquals(0, stubRepository.updateCategoryCount);
        assertEquals(1, stubRepository.updateCategoriesCount);
    }

    @Test
    void shouldReturnEmptyMapForExecuteAllWhenInputIsBlankOrNull() {
        assertTrue(useCase.executeAll(null).isEmpty());
        assertTrue(useCase.executeAll("").isEmpty());
        assertTrue(useCase.executeAll("   ").isEmpty());
        assertEquals(0, stubRepository.updateCategoriesCount);
    }

    @Test
    void shouldReturnCachedDataWhenAlreadySavedInMeshOntology() {
        Map<String, Boolean> precomputed = new HashMap<>();
        for (MeshCategory cat : MeshCategory.values()) {
            precomputed.put(cat.name(), cat == MeshCategory.LIGAND);
        }

        MeshInfo termWithCategories = MeshInfo.builder()
                .meshId("D007371")
                .name("Interferon-gamma")
                .synonyms(List.of("IFN-gamma"))
                .parents(List.of("D036341"))
                .categories(precomputed)
                .build();
        stubRepository.save(termWithCategories);

        Map<MeshCategory, Boolean> results = useCase.executeAll("D007371");

        assertEquals(MeshCategory.values().length, results.size());
        assertTrue(results.get(MeshCategory.LIGAND));
        assertFalse(results.get(MeshCategory.PROTEIN)); // saved as false in precomputed, should return cached
        assertEquals(0, stubRepository.updateCategoriesCount); // No DB write when all cached
    }

    @Test
    void shouldReturnCachedCategoryForExecuteWhenAlreadySaved() {
        Map<String, Boolean> precomputed = new HashMap<>();
        precomputed.put(MeshCategory.PROTEIN.name(), true);

        MeshInfo termWithCategories = MeshInfo.builder()
                .meshId("D99999")
                .name("Some Term")
                .synonyms(List.of())
                .parents(List.of())
                .categories(precomputed)
                .build();
        stubRepository.save(termWithCategories);

        // Even without parents or matches, cached true should be returned directly
        assertTrue(useCase.execute("D99999", MeshCategory.PROTEIN));
        assertEquals(0, stubRepository.updateCategoryCount); // No DB write when already cached
    }

    @Test
    void shouldReturnFalseAndNotUpdateDatabaseWhenTermDoesNotExistInExecute() {
        assertFalse(useCase.execute("NON_EXISTENT", MeshCategory.PROTEIN));
        assertEquals(0, stubRepository.updateCategoryCount);
    }

    @Test
    void shouldReturnAllFalseAndNotUpdateDatabaseWhenTermDoesNotExistInExecuteAll() {
        Map<MeshCategory, Boolean> results = useCase.executeAll("NON_EXISTENT");

        assertEquals(MeshCategory.values().length, results.size());
        results.values().forEach(isType -> assertFalse(isType));
        assertEquals(0, stubRepository.updateCategoriesCount);
    }

    @Test
    void shouldOnlyEvaluateMissingCategoriesWhenPartialCacheExists() {
        // Pre-cache only ENZYME=true
        Map<String, Boolean> partialCache = new HashMap<>();
        partialCache.put(MeshCategory.ENZYME.name(), true);

        MeshInfo term = MeshInfo.builder()
                .meshId("D002785")
                .name("Cholesterol 7-alpha-Hydroxylase")
                .synonyms(List.of("CYP7A1"))
                .parents(List.of("D000602"))
                .categories(partialCache)
                .build();
        stubRepository.save(term);
        stubRepository.save(createTerm("D000602", "Amino Acids, Peptides, and Proteins", List.of("Proteins"), List.of()));

        Map<MeshCategory, Boolean> results = useCase.executeAll("D002785");

        assertEquals(MeshCategory.values().length, results.size());
        assertTrue(results.get(MeshCategory.ENZYME)); // from partial cache
        assertTrue(results.get(MeshCategory.PROTEIN)); // from BFS evaluation
        assertFalse(results.get(MeshCategory.RECEPTOR));

        // Atomic write saved the complete categories map
        assertEquals(0, stubRepository.updateCategoryCount);
        assertEquals(1, stubRepository.updateCategoriesCount);
    }
}