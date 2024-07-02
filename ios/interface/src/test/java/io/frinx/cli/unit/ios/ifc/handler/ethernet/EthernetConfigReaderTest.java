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

package io.frinx.cli.unit.ios.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724.SPEEDAUTO;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED100MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;

class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG_AUTO_SPEED = """
            interface TenGigabitEthernet0/10
             no switchport
             ip address 192.168.0.1 255.255.255.0
             channel-group 20 mode active
            end

            """;

    private static final Config EXPECTED_CONFIG_AUTO_SPEED = new ConfigBuilder()
            .setPortSpeed(SPEEDAUTO.class)
            .build();

    private static final String SH_INT_CONFIG_100_SPEED = """
            interface FastEthernet0/20
             speed 100
            end

            """;

    private static final Config EXPECTED_CONFIG_100_SPEED = new ConfigBuilder()
            .setPortSpeed(SPEED100MB.class)
            .build();

    private static final String SH_INT_VLAN_CONFIG = """
            interface Vlan10
             description Internet
             no ip redirects
             no ip proxy-arp
            !
            """;

    private static final Config EXPECTED_VLAN_CONFIG = new ConfigBuilder()
            .build();

    @Test
    void testParseFastEthernet() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("TenGigabitEthernet0/10", SH_INT_CONFIG_AUTO_SPEED, configBuilder);
        assertEquals(new ConfigBuilder()
                .addAugmentation(Config1.class, new Config1Builder().setAggregateId("20").build())
                .addAugmentation(LacpEthConfigAug.class,
                        new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.ACTIVE).build())
                .build(), configBuilder.build());

        configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("FastEthernet0/20", SH_INT_CONFIG_100_SPEED, configBuilder);
        assertEquals(new ConfigBuilder().build(), configBuilder.build());
    }

    @Test
    void testParseVlan() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("Vlan10", SH_INT_VLAN_CONFIG, configBuilder);
        assertEquals(EXPECTED_VLAN_CONFIG, configBuilder.build());
    }

}
