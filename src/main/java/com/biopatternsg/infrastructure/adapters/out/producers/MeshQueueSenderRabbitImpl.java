package com.biopatternsg.infrastructure.adapters.out.producers;

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
    Emitter<String> emitter;

    @Override
    public void senderMeshId(String meshId) {
        emitter.send(meshId);
        log.info("Message sent to queue-ncbi: {}", meshId);
    }
}
