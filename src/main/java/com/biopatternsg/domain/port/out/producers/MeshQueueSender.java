package com.biopatternsg.domain.port.out.producers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;

public interface MeshQueueSender {
    void senderBiologicalObject(BiologicalObject biologicalObject);
}
