/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;

@RunWith(MockitoJUnitRunner.class)
public class VrfInterfaceReaderTest {

    private static  final String SH_IP_VRF_INTERFACES =
            "Interface              IP-Address      VRF                              Protocol\n" +
                    "GigabitEthernet1/0                  172.16.11.112      DEP_1                            up\n" +
                    "GigabitEthernet2/0                  172.16.11.113      DEP_1                            up";

    private static final List<InterfaceKey> IDS_EXPECTED =
            Lists.newArrayList("GigabitEthernet1/0", "GigabitEthernet2/0")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        assertEquals(IDS_EXPECTED, VrfInterfaceReader.parseInterfaceIds(SH_IP_VRF_INTERFACES));
    }

}