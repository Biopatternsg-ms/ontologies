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
