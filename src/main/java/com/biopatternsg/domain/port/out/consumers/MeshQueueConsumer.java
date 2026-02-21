package com.biopatternsg.domain.port.out.consumers;

import io.vertx.core.json.JsonObject;

public interface MeshQueueConsumer {
    void consumer(JsonObject jsonMsg);
}
