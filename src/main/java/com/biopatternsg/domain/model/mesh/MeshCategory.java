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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum MeshCategory {

    PROTEIN(
        List.of("PROTEIN", "PROTEINS"),
        (meshId, name, synonyms) -> hasMeshId(meshId, "D000602", "D011506", "D014157", "D004798")
                || containsText(name, synonyms, "protein", "enzyme", "transcription factor")
    ),

    ENZYME(
        List.of("ENZYME", "ENZYMES"),
        (meshId, name, synonyms) -> hasMeshId(meshId, "D004798")
                || containsText(name, synonyms, "enzyme")
    ),

    RECEPTOR(
        List.of("RECEPTOR", "RECEPTORS"),
        (meshId, name, synonyms) -> startsWithText(name, synonyms, "receptor")
                || containsText(name, synonyms, "receptor")
    ),

    LIGAND(
        List.of("LIGAND", "LIGANDS"),
        (meshId, name, synonyms) -> hasMeshId(meshId, "D036341")
                || containsText(name, synonyms, "Intercellular Signaling Peptides", "Signaling Peptides and Proteins", "ligand")
    ),

    TRANSCRIPTION_FACTOR(
        List.of("TRANSCRIPTION_FACTOR", "TRANSCRIPTION_FACTORS"),
        (meshId, name, synonyms) -> hasMeshId(meshId, "D014157")
                || containsText(name, synonyms, "transcription factor")
    ),

    ADAPTOR_PROTEIN(
        List.of("ADAPTOR_PROTEIN", "ADAPTOR_PROTEINS"),
        (meshId, name, synonyms) -> startsWithText(name, synonyms, "adaptor protein")
    );

    public static final String MESH_TREE_ROOT_ID = "1000048";

    @FunctionalInterface
    public interface CategoryMatcher {
        boolean test(String meshId, String name, List<String> synonyms);
    }

    private final List<String> aliases;
    private final CategoryMatcher matcher;

    MeshCategory(List<String> aliases, CategoryMatcher matcher) {
        this.aliases = aliases;
        this.matcher = matcher;
    }

    public boolean matches(String meshId, String name, List<String> synonyms) {
        return matcher.test(meshId, name, synonyms != null ? synonyms : Collections.emptyList());
    }

    public static Optional<MeshCategory> fromString(String type) {
        if (type == null || type.isBlank()) {
            return Optional.empty();
        }
        String normalized = type.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        return Arrays.stream(values())
                .filter(cat -> cat.aliases.contains(normalized))
                .findFirst();
    }

    public static boolean isRootTerm(String termId) {
        return termId != null && MESH_TREE_ROOT_ID.equals(termId.trim());
    }

    private static boolean hasMeshId(String meshId, String... targetIds) {
        if (meshId == null) return false;
        String upperMeshId = meshId.toUpperCase();
        return Arrays.stream(targetIds).anyMatch(id -> upperMeshId.equals(id.toUpperCase()));
    }

    private static boolean containsText(String name, List<String> synonyms, String... keywords) {
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            if (name != null && name.toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (synonyms != null && synonyms.stream().anyMatch(s -> s != null && s.toLowerCase().contains(lowerKeyword))) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithText(String name, List<String> synonyms, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        if (name != null && name.toLowerCase().startsWith(lowerPrefix)) {
            return true;
        }
        return synonyms != null && synonyms.stream().anyMatch(s -> s != null && s.toLowerCase().startsWith(lowerPrefix));
    }
}