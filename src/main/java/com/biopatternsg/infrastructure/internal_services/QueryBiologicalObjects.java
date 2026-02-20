package com.biopatternsg.infrastructure.internal_services;

import jakarta.ws.rs.core.Response;

public interface QueryBiologicalObjects {
    Response updateMeshId(String biologicalObjectId, String meshId);
}
