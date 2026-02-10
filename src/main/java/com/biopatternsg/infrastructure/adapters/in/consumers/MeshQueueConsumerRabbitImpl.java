package com.biopatternsg.infrastructure.adapters.in.consumers;

import com.biopatternsg.application.usecase.BuildMeshOntologyTreeUseCase;
import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.out.consumers.MeshQueueConsumer;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MeshQueueConsumerRabbitImpl implements MeshQueueConsumer {

    private final BuildMeshOntologyTreeUseCase buildMeshOntologyTreeUseCase;

    @Incoming("ncbi-in")
    @Override
    public void consumer(JsonObject jsonMsg) {
        BiologicalObject biologicalObject = jsonMsg.mapTo(BiologicalObject.class);
        buildMeshOntologyTreeUseCase.execute(biologicalObject);
    }

}
