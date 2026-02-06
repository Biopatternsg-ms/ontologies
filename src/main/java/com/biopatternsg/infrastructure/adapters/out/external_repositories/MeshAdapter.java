package com.biopatternsg.infrastructure.adapters.out.external_repositories;

import com.biopatternsg.domain.port.out.external_repositories.MeshOntologyRepoWeb;
import com.biopatternsg.infrastructure.external_services.QueryMesh;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@ApplicationScoped
public class MeshAdapter implements MeshOntologyRepoWeb {

    private final QueryMesh queryMesh;

    @Override
    public List<String> getTermIds(String biologicalObjectName) {
        return queryMesh.getTermIds(biologicalObjectName);
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        return queryMesh.getSummarySynonyms(meshTermId);
    }
}
