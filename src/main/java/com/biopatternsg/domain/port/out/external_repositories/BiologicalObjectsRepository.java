package com.biopatternsg.domain.port.out.external_repositories;

import jakarta.ws.rs.core.Response;

public interface BiologicalObjectsRepository {
    Response updateMeshId(String biologicalObjectId, String meshId);
}
