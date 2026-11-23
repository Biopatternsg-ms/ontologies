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
package com.biopatternsg.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogSanitizerTest {

    @Test
    void shouldReplaceLineBreaksToPreventLogInjection() {
        String malicious = "obj-1\nFAKE INFO: injected\r\nsecond line";
        assertEquals("obj-1_FAKE INFO: injected__second line", LogSanitizer.sanitize(malicious));
    }

    @Test
    void shouldHandleNullSafely() {
        assertEquals("null", LogSanitizer.sanitize(null));
    }

    @Test
    void shouldKeepCleanValuesUnchanged() {
        assertEquals("TP53", LogSanitizer.sanitize("TP53"));
    }

    @Test
    void shouldSanitizeCollectionRepresentations() {
        List<String> synonyms = List.of("TP53", "p53\ninjected");
        assertEquals("[TP53, p53_injected]", LogSanitizer.sanitize(synonyms));
    }
}
