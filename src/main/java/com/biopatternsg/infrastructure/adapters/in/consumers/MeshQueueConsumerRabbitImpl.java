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
package com.biopatternsg.infrastructure.adapters.in.consumers;

import com.biopatternsg.domain.model.mesh.BiologicalObject;
import com.biopatternsg.domain.port.in.BuildMeshOntologyTree;
import com.biopatternsg.shared.LogSanitizer;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MeshQueueConsumerRabbitImpl {

    private final BuildMeshOntologyTree buildMeshOntologyTree;

    @Incoming("ncbi-in")
    @Blocking
    public void consumer(JsonObject jsonMsg) {
        String safeJsonMsg = LogSanitizer.sanitize(jsonMsg);
        log.info("Message received from mesh-queue: {}", safeJsonMsg);
        try {
            BiologicalObject biologicalObject = jsonMsg.mapTo(BiologicalObject.class);
            String safeId = LogSanitizer.sanitize(biologicalObject.getId());
            String safeName = LogSanitizer.sanitize(biologicalObject.getName());
            log.info("Consuming BiologicalObject from mesh-queue: id={}, name={}, symbol={}, synonyms={}",
                    safeId,
                    safeName,
                    LogSanitizer.sanitize(biologicalObject.getSymbol()),
                    LogSanitizer.sanitize(biologicalObject.getSynonyms()));
            buildMeshOntologyTree.execute(biologicalObject);
            log.info("Finished processing BiologicalObject from mesh-queue: id={}, name={}",
                    safeId,
                    safeName);
        } catch (Exception e) {
            log.error("Error processing message from mesh-queue: {}", safeJsonMsg, e);
        }
    }

}
