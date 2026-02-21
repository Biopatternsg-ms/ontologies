package com.biopatternsg.infrastructure.adapters.out.producers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.out.producers.MeshQueueSender;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MeshQueueSenderRabbitImpl implements MeshQueueSender {

    @Inject
    @Channel("ncbi-out")
    Emitter<BiologicalObject> emitter;

    @Override
    public void senderBiologicalObject(BiologicalObject biologicalObject) {
        try {
            log.info("Attempting to send message to RabbitMQ: {}", biologicalObject.getName());
            emitter.send(biologicalObject);
            log.info("Message dispatched to queue-ncbi: {}", biologicalObject.getName());
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ: {}", biologicalObject.getName(), e);
            throw e;
        }
    }
}
