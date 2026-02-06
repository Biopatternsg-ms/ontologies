package com.biopatternsg.infrastructure.adapters.in.consumers;

import com.biopatternsg.application.usecase.CreateMeshTermUseCase;
import com.biopatternsg.domain.port.out.consumers.MeshQueueConsumer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MeshQueueConsumerRabbitImpl implements MeshQueueConsumer {

    private final CreateMeshTermUseCase createMeshTermUseCase;

    @Incoming("ncbi-in")
    @Override
    public void consumer(String msg) {
        createMeshTermUseCase.execute(msg);
    }
}
