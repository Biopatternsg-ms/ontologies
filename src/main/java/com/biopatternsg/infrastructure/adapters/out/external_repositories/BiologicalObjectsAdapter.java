package com.biopatternsg.infrastructure.adapters.out.external_repositories;

import com.biopatternsg.domain.port.out.external_repositories.BiologicalObjectsRepository;
import com.biopatternsg.infrastructure.internal_services.QueryBiologicalObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class BiologicalObjectsAdapter implements BiologicalObjectsRepository {

    private final QueryBiologicalObjects queryBiologicalObjects;

    @Override
    public Response updateMeshId(String biologicalObjectId, String meshId) {
        return queryBiologicalObjects.updateMeshId(biologicalObjectId, meshId);
    }
}
