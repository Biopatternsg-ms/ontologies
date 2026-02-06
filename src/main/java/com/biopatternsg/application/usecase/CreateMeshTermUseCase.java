package com.biopatternsg.application.usecase;

import com.biopatternsg.application.services.MeshOntologyService;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.model.mesh.MeshInfo;
import com.biopatternsg.domain.port.in.CreateMeshTerm;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.adapters.out.producers.MeshQueueSenderRabbitImpl;
import com.biopatternsg.infrastructure.external_services.dto.mesh.SummaryMesh;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class CreateMeshTermUseCase implements CreateMeshTerm {

    private final MeshOntologyService meshOntologyService;
    private final MeshOntologyRepository meshOntologyRepository;
    private final MeshQueueSenderRabbitImpl producer;

    @Override
    public void execute(String currentTermId) {

        if (isSavedTerm(currentTermId)) {
            log.info("Term {} already exists", currentTermId);
            return;
        }

        SummaryMesh currentSummaryMesh = getSummaryMesh(currentTermId);
        List<String> currentTermParents = currentSummaryMesh.getParents().stream().toList();

        List<String> currentSynonyms = currentSummaryMesh.getMeshSynonyms().stream().toList();

        MeshInfo nuevo = MeshInfo.builder()
                .meshId(currentTermId)
                .name(currentSynonyms.getFirst())
                .parents(currentTermParents)
                .synonyms(currentSynonyms.subList(1, currentSynonyms.size()))
                .build();

        saveMechTerm(nuevo, new BiologicalObject());
        log.info("Term {} saved", currentTermId);

        currentTermParents.forEach(producer::senderMeshId);
    }

    private void saveMechTerm(MeshInfo matchTermId, BiologicalObject biologicalObject) {
        MeshTermCollection collection = new MeshTermCollection();
        collection.setMeshId(matchTermId.getMeshId());
        collection.setName(matchTermId.getName());
        collection.setSymbol(biologicalObject.getSymbol());
        collection.setSynonyms(matchTermId.getSynonyms());
        collection.setParents(matchTermId.getParents());

        meshOntologyRepository.save(collection);
    }

    boolean isSavedTerm(String term) {
        Optional<MeshTermCollection> byName = meshOntologyRepository.findByMeshId(term);
        return byName.isPresent();
    }

    private SummaryMesh getSummaryMesh(String termId) {
        return meshOntologyService.getSummarySynonyms(termId);
    }

}
