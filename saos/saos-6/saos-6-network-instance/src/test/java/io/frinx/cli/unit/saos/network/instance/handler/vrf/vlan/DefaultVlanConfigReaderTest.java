/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class DefaultVlanConfigReaderTest {

    private static final String DEFAULT_OUTPUT = "";
    private static final String OUTPUT = "vlan rename vlan 5 name VLAN_5\n"
           + "vlan set vlan 5 egress-tpid 9100";

    @Test
    public void testParseVlanConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        Config1Builder config1Builder = new Config1Builder();
        VlanId vlanId = new VlanId(5);

        DefaultVlanConfigReader.parseVlanConfig(OUTPUT, configBuilder, config1Builder, vlanId);

        Assert.assertEquals(5, configBuilder.getVlanId().getValue().intValue());
        Assert.assertEquals("VLAN_5", configBuilder.getName());
        Assert.assertEquals(TPID0X9100.class, config1Builder.getEgressTpid());

        DefaultVlanConfigReader.parseVlanConfig(DEFAULT_OUTPUT, configBuilder, config1Builder, vlanId);

        Assert.assertEquals(5, configBuilder.getVlanId().getValue().intValue());
        Assert.assertEquals("VLAN#5", configBuilder.getName());
        Assert.assertEquals(TPID0X8100.class, config1Builder.getEgressTpid());
    }
}