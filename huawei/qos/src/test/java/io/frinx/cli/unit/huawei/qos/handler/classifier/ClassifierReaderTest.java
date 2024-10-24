/*
 * Copyright © 2021 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.frinx.cli.unit.huawei.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;


class ClassifierReaderTest {

    private static final String MAP_KEYS = """
            traffic classifier VOICE operator or
            traffic classifier NNI operator or
            traffic classifier VIDEO operator or""";

    private static final String DEFAULT_KEYS = """
            traffic policy TP-NNI-BACKUP-OUT
            traffic policy TP-NNI-MAIN-OUT
            traffic policy TP-ASSURED-OUT
            traffic policy TP-ASSURED-VOICE-OUT
            traffic policy TP-DEFAULT-VOICE-OUT
            """;

    @Test
    void testMapKeys() {
        final List<ClassifierKey> keys = ClassifierReader.getClassifierMapKeys(MAP_KEYS);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("VOICE", "NNI", "VIDEO"),
                keys.stream().map(ClassifierKey::getName).collect(Collectors.toList()));
    }

    @Test
    void testDefaultKeys() {
        final List<ClassifierKey> keys = ClassifierReader.getClassifierDefaultKeys(DEFAULT_KEYS);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("TP-NNI-BACKUP-OUT-default", "TP-NNI-MAIN-OUT-default",
                        "TP-ASSURED-OUT-default", "TP-ASSURED-VOICE-OUT-default", "TP-DEFAULT-VOICE-OUT-default"),
                keys.stream().map(ClassifierKey::getName).collect(Collectors.toList()));
    }
}
