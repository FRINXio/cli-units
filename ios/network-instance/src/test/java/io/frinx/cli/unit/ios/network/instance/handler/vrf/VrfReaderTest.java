/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

@RunWith(MockitoJUnitRunner.class)
public class VrfReaderTest {

    private static  final String SH_IP_VRF =
            "ip vrf DEP_1  \n" +
            "ip vrf DEP_2  dfs dsf dsf\n" +
            "ip vrf a\n";

    private static final List<NetworkInstanceKey> IDS_EXPECTED =
            Lists.newArrayList("DEP_1", "DEP_2", "a", "default")
                    .stream()
                    .map(NetworkInstanceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        assertEquals(IDS_EXPECTED, VrfReader.parseVrfIds(SH_IP_VRF));
    }

}