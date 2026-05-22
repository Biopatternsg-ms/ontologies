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

import com.biopatternsg.domain.port.in.BuildMeshOntologyTree;
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

    private final BuildMeshOntologyTree buildMeshOntologyTree;

    @Incoming("ncbi-in")
    @Override
    public void consumer(JsonObject jsonMsg) {
        BiologicalObject biologicalObject = jsonMsg.mapTo(BiologicalObject.class);
        buildMeshOntologyTree.execute(biologicalObject);
    }

}
