package com.biopatternsg.application.services.impl;

import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.port.out.external_repositories.MeshOntologyRepoWeb;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class MeshOntologyServiceImpl implements MeshOntologyService {

    private final MeshOntologyRepoWeb meshOntologyRepoWeb;

    @Override
    public List<String> getMeshTermIds(String objectName) {
        return meshOntologyRepoWeb.getTermIds(objectName);
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        return meshOntologyRepoWeb.getSummarySynonyms(meshTermId);
    }
}