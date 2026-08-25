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
package com.biopatternsg.domain.model.mesh;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MeshCategoryTest {

    @Test
    void shouldFindCategoryFromDifferentStringFormats() {
        assertEquals(Optional.of(MeshCategory.PROTEIN), MeshCategory.fromString("protein"));
        assertEquals(Optional.of(MeshCategory.PROTEIN), MeshCategory.fromString("PROTEINS"));
        assertEquals(Optional.of(MeshCategory.PROTEIN), MeshCategory.fromString("  protein  "));

        assertEquals(Optional.of(MeshCategory.ENZYME), MeshCategory.fromString("enzyme"));
        assertEquals(Optional.of(MeshCategory.ENZYME), MeshCategory.fromString("ENZYMES"));

        assertEquals(Optional.of(MeshCategory.RECEPTOR), MeshCategory.fromString("receptor"));
        assertEquals(Optional.of(MeshCategory.RECEPTOR), MeshCategory.fromString("RECEPTORS"));

        assertEquals(Optional.of(MeshCategory.LIGAND), MeshCategory.fromString("ligand"));
        assertEquals(Optional.of(MeshCategory.LIGAND), MeshCategory.fromString("LIGANDS"));

        assertEquals(Optional.of(MeshCategory.TRANSCRIPTION_FACTOR), MeshCategory.fromString("transcription_factor"));
        assertEquals(Optional.of(MeshCategory.TRANSCRIPTION_FACTOR), MeshCategory.fromString("TRANSCRIPTION-FACTOR"));
        assertEquals(Optional.of(MeshCategory.TRANSCRIPTION_FACTOR), MeshCategory.fromString("transcription factor"));

        assertEquals(Optional.of(MeshCategory.ADAPTOR_PROTEIN), MeshCategory.fromString("adaptor_protein"));
        assertEquals(Optional.of(MeshCategory.ADAPTOR_PROTEIN), MeshCategory.fromString("adaptor protein"));

        assertTrue(MeshCategory.fromString("unknown_category").isEmpty());
        assertTrue(MeshCategory.fromString(null).isEmpty());
        assertTrue(MeshCategory.fromString("   ").isEmpty());
    }

    @Test
    void shouldMatchProteinByMeshIdOrNameOrSynonyms() {
        assertTrue(MeshCategory.PROTEIN.matches("D000602", "Some Name", List.of()));
        assertTrue(MeshCategory.PROTEIN.matches("D011506", "Some Name", List.of()));
        assertTrue(MeshCategory.PROTEIN.matches("OTHER", "Membrane Proteins", List.of()));
        assertTrue(MeshCategory.PROTEIN.matches("OTHER", "Other Name", List.of("Cytochrome P450 Protein")));
        assertFalse(MeshCategory.PROTEIN.matches("OTHER", "Cholesterol", List.of("Lipid")));
    }

    @Test
    void shouldMatchEnzymeByMeshIdOrNameOrSynonyms() {
        assertTrue(MeshCategory.ENZYME.matches("D004798", "Enzymes", List.of()));
        assertTrue(MeshCategory.ENZYME.matches("OTHER", "Oxidoreductase Enzyme", List.of()));
        assertTrue(MeshCategory.ENZYME.matches("OTHER", "Hydrolase", List.of("Digestive Enzyme")));
        assertFalse(MeshCategory.ENZYME.matches("OTHER", "Cholesterol", List.of("Sterol")));
    }

    @Test
    void shouldMatchReceptorByNameStartingWithReceptor() {
        assertTrue(MeshCategory.RECEPTOR.matches("D011930", "Receptors, Cell Surface", List.of()));
        assertTrue(MeshCategory.RECEPTOR.matches("OTHER", "Receptor-like Kinase", List.of()));
        assertTrue(MeshCategory.RECEPTOR.matches("OTHER", "Nuclear Receptor", List.of("Receptors, Nuclear")));
        assertFalse(MeshCategory.RECEPTOR.matches("OTHER", "Ligand", List.of()));
    }

    @Test
    void shouldMatchLigandByMeshIdOrIntercellularSignaling() {
        assertTrue(MeshCategory.LIGAND.matches("D036341", "Intercellular Signaling Peptides and Proteins", List.of()));
        assertTrue(MeshCategory.LIGAND.matches("OTHER", "Signaling Peptides and Proteins", List.of()));
        assertTrue(MeshCategory.LIGAND.matches("OTHER", "Chemical Ligand", List.of()));
        assertFalse(MeshCategory.LIGAND.matches("OTHER", "Receptor", List.of()));
    }

    @Test
    void shouldCorrectlyIdentifyRootTerm() {
        assertTrue(MeshCategory.isRootTerm("1000048"));
        assertTrue(MeshCategory.isRootTerm("  1000048  "));
        assertEquals("1000048", MeshCategory.MESH_TREE_ROOT_ID);
        assertFalse(MeshCategory.isRootTerm("D002784"));
        assertFalse(MeshCategory.isRootTerm(null));
        assertFalse(MeshCategory.isRootTerm(""));
    }
}