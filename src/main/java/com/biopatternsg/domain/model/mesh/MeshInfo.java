package com.biopatternsg.domain.model.mesh;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class MeshInfo {
    private String meshId;
    private String name;
    private List<String> synonyms;
    private List<String> parents;
}
