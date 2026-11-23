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
package com.biopatternsg.infrastructure.external_services.dto.mesh;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ESummaryResultTest {

    @Test
    void getValuesBySubItemNameHandlesNullContentAndNullSubItemName() {
        ESummaryResult.Item item = new ESummaryResult.Item();
        item.setContent(null);

        assertTrue(item.getValuesBySubItemName("Parent").isEmpty());
        assertTrue(item.getValuesBySubItemName(null).isEmpty());
        assertTrue(item.getValuesBySubItemNameOrText("string").isEmpty());
    }

    @Test
    void getValuesBySubItemNameExtractsMatchingChildrenSafely() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element wrapper = doc.createElement("Item");
        Element match = doc.createElement("Item");
        match.setAttribute("Name", "Parent");
        match.setTextContent("  12345  ");
        Element other = doc.createElement("Item");
        other.setAttribute("Name", "Other");
        other.setTextContent("ignored");
        wrapper.appendChild(match);
        wrapper.appendChild(other);

        ESummaryResult.Item item = new ESummaryResult.Item();
        item.setContent(List.of(wrapper));

        assertEquals(List.of("12345"), item.getValuesBySubItemName("Parent"));
    }

    @Test
    void getValuesBySubItemNameOrTextFallsBackToElementTextWhenNoChildItems() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element leaf = doc.createElement("Item");
        leaf.setTextContent("  Neoplasm  ");

        ESummaryResult.Item item = new ESummaryResult.Item();
        item.setContent(List.of(leaf));

        assertEquals(List.of("Neoplasm"), item.getValuesBySubItemNameOrText("string"));
    }
}
