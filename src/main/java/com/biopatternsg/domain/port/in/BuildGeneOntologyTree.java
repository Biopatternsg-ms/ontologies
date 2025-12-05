package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.model.GeneOntologyBuildTreeRequest;

public interface BuildGeneOntologyTree {
    void execute(GeneOntologyBuildTreeRequest geneOntology);
}
