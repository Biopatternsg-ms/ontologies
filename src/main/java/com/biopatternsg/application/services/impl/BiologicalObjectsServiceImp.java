package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.BiologicalObjectsService;
import com.biopatternsg.domain.port.out.external_repositories.BiologicalObjectsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BiologicalObjectsServiceImp implements BiologicalObjectsService {

    private final BiologicalObjectsRepository biologicalObjectsRepository;

    @Override
    public void updateMeshId(String biologicalObjectId, String meshId) {
        Response response = biologicalObjectsRepository.updateMeshId(biologicalObjectId, meshId);
        log.info(response.readEntity(String.class));
    }
}
