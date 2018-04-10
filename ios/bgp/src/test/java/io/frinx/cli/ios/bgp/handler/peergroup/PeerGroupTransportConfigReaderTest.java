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

package io.frinx.cli.ios.bgp.handler.peergroup;

import static org.junit.Assert.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.transport.ConfigBuilder;

public class PeerGroupTransportConfigReaderTest {

    private static final String OUTPUT = "router bgp 65000\n" +
            " neighbor group-for-some-neighbors peer-group\n" +
            " neighbor group-for-some-neighbors remote-as 45\n" +
            " neighbor group-for-some-neighbors update-source Loopback0\n" +
            " neighbor group-for-some-neighbors transport connection-mode passive\n" +
            " neighbor group-for-some-neighbors activate\n";

    private static final String OUTPUT2 = "router bgp 65000\n" +
            " neighbor group-for-some-neighbors peer-group\n" +
            " neighbor group-for-some-neighbors remote-as 45\n" +
            " neighbor group-for-some-neighbors activate\n";


    @Test
    public void testParse() throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder();
        PeerGroupTransportConfigReader.parseConfigAttributes(OUTPUT, configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                        .setPassiveMode(true)
                        .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                        .build(),
                configBuilder.build());

        configBuilder = new ConfigBuilder();
        PeerGroupTransportConfigReader.parseConfigAttributes(OUTPUT2, configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                        .setPassiveMode(false)
                        .build(),
                configBuilder.build());
    }
}