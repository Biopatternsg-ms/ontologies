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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "eSummaryResult")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ESummaryResult {
    @XmlElement(name = "DocSum")
    private DocSum docSum;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class DocSum {
        @XmlElement(name = "Id")
        private String id;

        @XmlElement(name = "Item")
        private List<Item> items;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Item {
        @XmlAttribute(name = "Name")
        private String name;

        @XmlAttribute(name = "Type")
        private String type;

        @XmlAnyElement(lax = true)
        @XmlMixed
        private List<Object> content = new ArrayList<>();

        public List<String> getValuesBySubItemName(String subItemName) {
            List<String> values = new ArrayList<>();
            if (content == null || subItemName == null) {
                return values;
            }
            for (Object obj : content) {
                if (obj instanceof Element element) {
                    collectMatchingSubItemValues(element, subItemName, values);
                }
            }
            return values;
        }

        public List<String> getValuesBySubItemNameOrText(String subItemName) {
            List<String> values = new ArrayList<>();
            if (content == null) {
                return values;
            }
            for (Object obj : content) {
                if (obj instanceof Element element) {
                    var children = element.getElementsByTagName("Item");
                    if (children.getLength() == 0) {
                        addTrimmedText(element.getTextContent(), values);
                    } else if (subItemName != null) {
                        collectMatchingSubItemValues(element, subItemName, values);
                    }
                }
            }
            return values;
        }

        private void collectMatchingSubItemValues(Element element, String subItemName, List<String> values) {
            var children = element.getElementsByTagName("Item");
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element child && subItemName.equals(child.getAttribute("Name"))) {
                    addTrimmedText(child.getTextContent(), values);
                }
            }
        }

        private void addTrimmedText(String text, List<String> values) {
            if (text != null) {
                values.add(text.trim());
            }
        }
    }
}
