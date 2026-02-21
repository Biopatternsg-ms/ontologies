package com.biopatternsg.application.usecase;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.in.SendBiologicalObjectToQueue;
import com.biopatternsg.domain.port.out.repositories.MeshOntologyRepository;
import com.biopatternsg.infrastructure.adapters.out.producers.MeshQueueSenderRabbitImpl;
import com.biopatternsg.infrastructure.mongo.MeshTermCollection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SendBiologicalObjectToQueueUseCase implements SendBiologicalObjectToQueue {

    private final MeshOntologyRepository meshOntologyRepository;
    private final MeshQueueSenderRabbitImpl meshRabbitSender;

    @Override
    public void execute(BiologicalObject biologicalObject) {

        if (isSavedName(biologicalObject.getName()) || isSavedSynonyms(biologicalObject.getSynonyms())) {
            log.info("Mesh tree for {} already exists", biologicalObject.getName());
            return;
        }

        meshRabbitSender.senderBiologicalObject(biologicalObject);
    }

    private boolean isSavedSynonyms(List<String> synonyms) {
        Optional<MeshTermCollection> byName = meshOntologyRepository.findBySynonyms(synonyms);
        return byName.isPresent();
    }

    boolean isSavedName(String name) {
        Optional<MeshTermCollection> byName = meshOntologyRepository.findByName(name);
        return byName.isPresent();
    }

}
