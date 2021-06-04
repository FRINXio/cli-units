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

package io.frinx.cli.unit.iosxe.bgp.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public class GlobalAfiSafiConfigReaderTest {

    private static final String OUTPUT = "router bgp 65333\n"
            + " address-family ipv4\n"
            + "  no auto-summary\n"
            + " exit-address-family\n"
            + " address-family ipv6\n"
            + "  redistribute connected\n"
            + "  redistribute static\n"
            + " exit-address-family\n"
            + " address-family ipv4 vrf iaksip\n"
            + " exit-address-family\n"
            + " address-family ipv4 vrf VLAN372638\n"
            + "  synchronization\n"
            + "  redistribute connected\n"
            + "  redistribute static\n"
            + "  default-information originate\n"
            + " exit-address-family\n"
            + " address-family ipv4 vrf CIA-SINGLE1\n"
            + "  synchronization\n"
            + "  redistribute static\n"
            + " exit-address-family\n"
            + " address-family ipv4 vrf CIA-SINGLE\n"
            + " exit-address-family\n";

    @Test
    public void setAutoSummaryTest() {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalAfiSafiConfigReader.parseConfig(OUTPUT, new NetworkInstanceKey("default"), IPV4UNICAST.class, builder);
        Assert.assertEquals(false, builder.getAugmentation(GlobalAfiSafiConfigAug.class).isAutoSummary());
    }

    @Test
    public void parseConfigTest() {
        // default network instance
        buildAndTest("default", IPV4UNICAST.class, false, null, null, null, null);

        // VLAN372638 network instance
        buildAndTest("VLAN372638", IPV4UNICAST.class, null, true, true, true, true);

        // CIA-SINGLE network instance
        buildAndTest("CIA-SINGLE", IPV4UNICAST.class, null, false, false, false, false);

        // CIA-SINGLE1 network instance
        buildAndTest("CIA-SINGLE1", IPV4UNICAST.class, null, false, true, false, true);
    }

    private void buildAndTest(String vrfKey,
                              Class<? extends AFISAFITYPE> afiSafiName,
                              Boolean expectedAutoSummary,
                              Boolean expectedRedistributeConnected,
                              Boolean expectedRedistributeStatic,
                              Boolean expectedDefaultInformation,
                              Boolean expectedSynchronization) {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalAfiSafiConfigReader.parseConfig(OUTPUT, new NetworkInstanceKey(vrfKey), afiSafiName, builder);
        GlobalAfiSafiConfigAug configAug = builder.getAugmentation(GlobalAfiSafiConfigAug.class);

        Assert.assertEquals(expectedAutoSummary, configAug.isAutoSummary());
        Assert.assertEquals(expectedRedistributeConnected, configAug.isRedistributeConnected());
        Assert.assertEquals(expectedRedistributeStatic, configAug.isRedistributeStatic());
        Assert.assertEquals(expectedDefaultInformation, configAug.isDefaultInformationOriginate());
        Assert.assertEquals(expectedSynchronization, configAug.isSynchronization());
    }
}
