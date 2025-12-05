package com.biopatternsg.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PathsToRoot {
    int numberOfHits;
    List<List<PathToRoot>> results;
}
