/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.ios.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.IosQosClassifierAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.ConfigBuilder;

class ClassifierConfigReaderTest {

    private static final String MAP_ALL = """
             Class Map match-all TEST (id 1)

               Match qos-group 6
               Match ip  dscp 52 \
            """;

    private static final String MAP_ANY = """
              Class Map match-any CLASS-NNI-VIDEO (id 7)

               Match cos  3\s
            """;

    @Test
    void testAll() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        ClassifierConfigReader.parseQosConfig(MAP_ALL, configBuilder);
        final IosQosClassifierAug aug = configBuilder.getAugmentation(IosQosClassifierAug.class);
        assertEquals("all", aug.getStatementsMatching());
    }

    @Test
    void testAny() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        ClassifierConfigReader.parseQosConfig(MAP_ANY, configBuilder);
        final IosQosClassifierAug aug = configBuilder.getAugmentation(IosQosClassifierAug.class);
        assertEquals("any",  aug.getStatementsMatching());
    }
}
