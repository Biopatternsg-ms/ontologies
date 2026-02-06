package com.biopatternsg.domain.model.mesh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "eSearchResult")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ESearchResult {
    @XmlElement(name = "Count")
    private String count;

    @XmlElement(name = "IdList")
    private IdListWrapper idList;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IdListWrapper {

        @XmlElement(name = "Id")
        private List<String> ids;

        public List<String> getIds() { return ids; }
        public void setIds(List<String> ids) { this.ids = ids; }
    }
}
