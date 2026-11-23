/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.biopatternsg.infrastructure.adapters.out.producers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.out.producers.MeshQueueSender;
import com.biopatternsg.shared.LogSanitizer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
public class MeshQueueSenderRabbitAdapter implements MeshQueueSender {

    private final Emitter<BiologicalObject> emitter;

    public MeshQueueSenderRabbitAdapter(@Channel("ncbi-out") Emitter<BiologicalObject> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void senderBiologicalObject(BiologicalObject biologicalObject) {
        String safeName = LogSanitizer.sanitize(biologicalObject.getName());
        try {
            log.info("Attempting to send message to RabbitMQ: {}", safeName);
            emitter.send(biologicalObject);
            log.info("Message dispatched to queue-ncbi: {}", safeName);
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ: {}", safeName, e);
            throw e;
        }
    }
}
