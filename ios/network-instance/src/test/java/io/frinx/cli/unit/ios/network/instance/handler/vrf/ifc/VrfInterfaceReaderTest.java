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

package io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;

@RunWith(MockitoJUnitRunner.class)
public class VrfInterfaceReaderTest {

    private static final String SH_IP_VRF_INTERFACES = "interface Loopback0\n"
            + "interface Loopback1\n"
            + " ip vrf forwarding a\n"
            + "interface Loopback44\n"
            + "interface GigabitEthernet1\n"
            + "interface GigabitEthernet2\n"
            + "interface GigabitEthernet3\n";

    private static final List<InterfaceKey> IDS_EXPECTED =
            Lists.newArrayList("Loopback0", "Loopback44", "GigabitEthernet1", "GigabitEthernet2", "GigabitEthernet3")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_EXPECTED_VRF =
            Lists.newArrayList("Loopback1")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        assertEquals(IDS_EXPECTED, VrfInterfaceReader.parseIds(NetworInstance.DEFAULT_NETWORK_NAME,
                SH_IP_VRF_INTERFACES));
        assertEquals(IDS_EXPECTED_VRF, VrfInterfaceReader.parseIds("a", SH_IP_VRF_INTERFACES));
        assertEquals(Collections.emptyList(), VrfInterfaceReader.parseIds("b", SH_IP_VRF_INTERFACES));
    }

}