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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.InterfaceKey;


public class InterfaceReaderTest {

    private static final String OUTPUT = "-------------------------------------------------\n"
            + "  Policy Name:   TP-NNI-MAIN-OUT \n"
            + "  Policy Index:  1\n"
            + "     Classifier:NNI     Behavior:NNI-MAIN     Precedence:5\n"
            + "-------------------------------------------------\n"
            + " *interface GigabitEthernet0/0/4.100\n"
            + "    traffic-policy TP-NNI-MAIN-OUT inbound  \n"
            + "      slot 0    :  apply-fail\n"
            + "Error: Only statistics and CAR are supported in Inbound-HQoS and either of them is a MUST, \n"
            + "      nest Policy :  TP-DEFAULT-VOICE-OUT\n"
            + "      slot 0    :  apply-fail\n"
            + "-------------------------------------------------\n"
            + "  Policy Name:   TP-NNI-MAIN-OUT \n"
            + "  Policy Index:  1\n"
            + "     Classifier:NNI     Behavior:NNI-MAIN     Precedence:5\n"
            + "-------------------------------------------------\n"
            + " *interface GigabitEthernet0/0/0.100\n"
            + "    traffic-policy TP-NNI-MAIN-OUT outbound  \n"
            + "      slot 0    :  success\n"
            + "      nest Policy :  TP-DEFAULT-VOICE-VIDEO-OUT\n"
            + "      slot 0    :  success\n"
            + "-------------------------------------------------";

    @Test
    public void testIds() {
        final List<InterfaceKey> allIds = InterfaceReader.getAllIds(OUTPUT);
        Assert.assertEquals(2, allIds.size());
        Assert.assertEquals("GigabitEthernet0/0/0.100", allIds.get(0).getInterfaceId());
        Assert.assertEquals("GigabitEthernet0/0/4.100", allIds.get(1).getInterfaceId());
    }
}
