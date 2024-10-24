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
package io.frinx.cli.unit.huawei.qos.handler.ifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.InterfaceKey;


class InterfaceReaderTest {

    private static final String OUTPUT = """
            -------------------------------------------------
              Policy Name:   TP-NNI-MAIN-OUT\s
              Policy Index:  1
                 Classifier:NNI     Behavior:NNI-MAIN     Precedence:5
            -------------------------------------------------
             *interface GigabitEthernet0/0/4.100
                traffic-policy TP-NNI-MAIN-OUT inbound \s
                  slot 0    :  apply-fail
            Error: Only statistics and CAR are supported in Inbound-HQoS and either of them is a MUST,\s
                  nest Policy :  TP-DEFAULT-VOICE-OUT
                  slot 0    :  apply-fail
            -------------------------------------------------
              Policy Name:   TP-NNI-MAIN-OUT\s
              Policy Index:  1
                 Classifier:NNI     Behavior:NNI-MAIN     Precedence:5
            -------------------------------------------------
             *interface GigabitEthernet0/0/0.100
                traffic-policy TP-NNI-MAIN-OUT outbound \s
                  slot 0    :  success
                  nest Policy :  TP-DEFAULT-VOICE-VIDEO-OUT
                  slot 0    :  success
            -------------------------------------------------""";

    @Test
    void testIds() {
        final List<InterfaceKey> allIds = InterfaceReader.getAllIds(OUTPUT);
        assertEquals(2, allIds.size());
        assertEquals("GigabitEthernet0/0/0.100", allIds.get(0).getInterfaceId());
        assertEquals("GigabitEthernet0/0/4.100", allIds.get(1).getInterfaceId());
    }
}
