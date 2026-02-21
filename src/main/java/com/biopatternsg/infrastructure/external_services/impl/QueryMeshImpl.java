package com.biopatternsg.infrastructure.external_services.impl;

import com.biopatternsg.domain.model.mesh.ESearchResult;
import com.biopatternsg.domain.model.mesh.ESummaryResult;
import com.biopatternsg.infrastructure.clients.external_clients.MeshOntologyHttpClient;
import com.biopatternsg.infrastructure.external_services.QueryMesh;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class QueryMeshImpl implements QueryMesh {

    private final MeshOntologyHttpClient meshHttpClient;

    public QueryMeshImpl(@RestClient MeshOntologyHttpClient meshHttpClient) {
        this.meshHttpClient = meshHttpClient;
    }

    //TODO: Agregar lib de reciliencia
    @Override
    public List<String> getTermIds(String biologicalObjectName) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ESearchResult result = meshHttpClient.search(
                "mesh",
                biologicalObjectName,
                10,
                "biomed3"
            );

        return result.getIdList().getIds();
    }

    @Override
    public SummaryMesh getSummarySynonyms(String meshTermId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ESummaryResult result = meshHttpClient.summary("mesh", meshTermId);

        Set<String> allParents = result.getDocSum().getItems().stream()
                .filter(it -> "DS_IdxLinks".equals(it.getName()))
                .flatMap(it -> it.getValuesBySubItemName("Parent").stream())
                .collect(Collectors.toSet());

        Set<String> itemsIds = result.getDocSum().getItems().stream()
                .filter(it -> "DS_MeshTerms".equals(it.getName()))
                .flatMap(it -> it.getValuesBySubItemNameOtra("string").stream())
                .collect(Collectors.toSet());

        return SummaryMesh.builder()
                .parents(allParents)
                .meshSynonyms(itemsIds)
                .build();
    }




}
