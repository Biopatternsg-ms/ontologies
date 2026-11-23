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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MeshQueueSenderRabbitAdapter implements MeshQueueSender {

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
