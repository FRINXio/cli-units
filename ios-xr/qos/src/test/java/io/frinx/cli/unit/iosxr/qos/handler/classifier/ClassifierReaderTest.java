/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;

class ClassifierReaderTest {

    private static final String INPUT_ALL = """
            Fri Mar  9 12:54:59.445 UTC
            class-map match-any map1
            class-map match-all map2
            """;

    private static String INPUT_DEF_ALL = """
            Thu Mar 22 13:33:38.062 UTC
            policy-map plmap
            policy-map plmap1
            policy-map plmap2
            """;

    @Test
    void testIds() {
        List<ClassifierKey> keys = ClassifierReader.getClassifierMapKeys(INPUT_ALL);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("map1", "map2"),
                keys.stream()
                        .map(ClassifierKey::getName)
                        .collect(Collectors.toList()));

        keys = ClassifierReader.getClassifierDefaultKeys(INPUT_DEF_ALL);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("plmap-default", "plmap1-default", "plmap2-default"),
                keys.stream()
                        .map(ClassifierKey::getName)
                        .collect(Collectors.toList()));
    }
}