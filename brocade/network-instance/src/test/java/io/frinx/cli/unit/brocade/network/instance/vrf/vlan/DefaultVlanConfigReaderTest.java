/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class DefaultVlanConfigReaderTest {

    private static final String OUTPUT1 = "vlan 1500 \n"
            + " tagged e 2/11 \n"
            + " router-interface ve 150\n"
            + "!\n"
            + "vlan 2077 name IPTV-Management \n"
            + " router-interface ve 7\n"
            + " spanning-tree priority 0\n"
            + "!\n"
            + "vlan 4090 \n"
            + "!\n"
            + "\n"
            + "!\n";

    private static final String OUTPUT2 = "vlan 4090 name abcd\n!";

    @Test
    public void testParseConfig() {
        ConfigBuilder parsedBuilder = new ConfigBuilder();
        DefaultVlanConfigReader.parseVlanConfig(OUTPUT1, parsedBuilder, new VlanId(1500));
        Assert.assertEquals(new ConfigBuilder()
                .setVlanId(new VlanId(1500))
                .setStatus(VlanConfig.Status.ACTIVE)
                .build(), parsedBuilder.build());

        parsedBuilder = new ConfigBuilder();
        DefaultVlanConfigReader.parseVlanConfig(OUTPUT2, parsedBuilder, new VlanId(4090));
        Assert.assertEquals(new ConfigBuilder()
                .setVlanId(new VlanId(4090))
                .setStatus(VlanConfig.Status.ACTIVE)
                .setName("abcd")
                .build(), parsedBuilder.build());
    }
}