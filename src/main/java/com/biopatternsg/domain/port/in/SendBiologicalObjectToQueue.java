package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.model.mesh.BiologicalObject;

public interface SendBiologicalObjectToQueue {
    void execute(BiologicalObject biologicalObject);
}
