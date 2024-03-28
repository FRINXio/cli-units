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

package io.frinx.cli.unit.ios.bgp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

class GlobalAfiSafiConfigReaderTest {

    private static final String OUTPUT = """
            router bgp 65333
             address-family ipv4
              no auto-summary
             exit-address-family
             address-family ipv6
              redistribute connected
              redistribute static
             exit-address-family
             address-family ipv4 vrf iaksip
             exit-address-family
             address-family ipv4 vrf VLAN372638
              synchronization
              redistribute connected route-map FOO
              redistribute static route-map BAR
              default-information originate
             exit-address-family
             address-family ipv4 vrf CIA-SINGLE1
              synchronization
              redistribute static
             exit-address-family
             address-family ipv4 vrf CIA-SINGLE
             exit-address-family
            """;

    @Test
    void setAutoSummaryTest() {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalAfiSafiConfigReader.parseConfig(OUTPUT, new NetworkInstanceKey("default"), IPV4UNICAST.class, builder);
        assertEquals(false, builder.getAugmentation(GlobalAfiSafiConfigAug.class).isAutoSummary());
    }

    @Test
    void parseConfigTest() {
        // default network instance
        buildAndTest("default", IPV4UNICAST.class, false, null, null, null, null, null, null);

        // VLAN372638 network instance
        buildAndTest("VLAN372638", IPV4UNICAST.class, null, true, "FOO", true, "BAR", true, true);

        // CIA-SINGLE network instance
        buildAndTest("CIA-SINGLE", IPV4UNICAST.class, null, false, null, false, null, false, false);

        // CIA-SINGLE1 network instance
        buildAndTest("CIA-SINGLE1", IPV4UNICAST.class, null, false, null, true, null, false, true);
    }

    private void buildAndTest(String vrfKey,
                              Class<? extends AFISAFITYPE> afiSafiName,
                              Boolean expectedAutoSummary,
                              Boolean expectedRedistributeConnectedEnabled,
                              String expectedRedistributeConnectedRouteMap,
                              Boolean expectedRedistributeStaticEnabled,
                              String expectedRedistributeStaticRouteMap,
                              Boolean expectedDefaultInformation,
                              Boolean expectedSynchronization) {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalAfiSafiConfigReader.parseConfig(OUTPUT, new NetworkInstanceKey(vrfKey), afiSafiName, builder);
        GlobalAfiSafiConfigAug configAug = builder.getAugmentation(GlobalAfiSafiConfigAug.class);

        assertEquals(expectedAutoSummary, configAug.isAutoSummary());

        if (expectedRedistributeConnectedEnabled == null) {
            assertNull(configAug.getRedistributeConnected());
        } else {
            assertEquals(expectedRedistributeConnectedEnabled, configAug.getRedistributeConnected().isEnabled());
            assertEquals(expectedRedistributeConnectedRouteMap,
                    configAug.getRedistributeConnected().getRouteMap());
        }

        if (expectedRedistributeStaticEnabled == null) {
            assertNull(configAug.getRedistributeStatic());
        } else {
            assertEquals(expectedRedistributeStaticEnabled, configAug.getRedistributeStatic().isEnabled());
            assertEquals(expectedRedistributeStaticRouteMap, configAug.getRedistributeStatic().getRouteMap());
        }

        assertEquals(expectedDefaultInformation, configAug.isDefaultInformationOriginate());
        assertEquals(expectedSynchronization, configAug.isSynchronization());
    }
}
