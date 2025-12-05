package com.biopatternsg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
@Setter
public class GeneOntologyBuildTreeRequest {
    private List<String> cellularComponent;
    private List<String> molecularFunction;
    private List<String> biologicalProcess;

    public List<String> getGoTermIds() {
        return Stream.of(cellularComponent, molecularFunction, biologicalProcess)
                .flatMap(List::stream)
                .collect(Collectors.toSet())
                .stream()
                .toList();
    }
}
