package com.biopatternsg.domain.model.mesh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.w3c.dom.Element;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Node;

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

        public List<String> getSubItemValues() {
            List<String> values = new ArrayList<>();
            if (content == null) return values;

            for (Object obj : content) {
                if (obj instanceof Element element) {
                    String textValue = element.getFirstChild().getNodeValue();
                    if (textValue != null && !textValue.isBlank()) {
                        values.add(textValue.trim());
                    }
                }
            }
            return values;
        }

        public List<String> getValuesBySubItemName(String subItemName) {
            List<String> values = new ArrayList<>();
            for (Object obj : content) {
                if (obj instanceof Element element) {
                    // Buscamos nodos hijos que coincidan con el nombre de atributo
                    var children = element.getElementsByTagName("Item");
                    for (int i = 0; i < children.getLength(); i++) {
                        Element child = (Element) children.item(i);
                        if (subItemName.equals(child.getAttribute("Name"))) {
                            values.add(child.getTextContent().trim());
                        }
                    }
                }
            }
            return values;
        }

        public List<String> getValuesBySubItemNameOtra(String subItemName) {
            List<String> values = new ArrayList<>();
            for (Object obj : content) {
                if (obj instanceof Element element) {
                    var children = element.getElementsByTagName("Item");

                    if(children.getLength() == 0){
                        values.add(element.getTextContent().trim());
                    }

                    for (int i = 0; i < children.getLength(); i++) {
                        Element child = (Element) children.item(i);
                        if (subItemName.equals(child.getAttribute("Name"))) {
                            values.add(child.getTextContent().trim());
                        }
                    }
                }
            }
            return values;
        }

    }



}
