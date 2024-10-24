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

package io.frinx.cli.unit.iosxe.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfEthExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfEthCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfEthCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED100MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;

class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG_AUTO_SPEED = """
            interface GigabitEthernet0/0/0
             no switchport
             ip address 192.168.0.1 255.255.255.0
             lacp rate fast
             channel-group 20 mode active
            end

            """;

    private static final String SH_INT_CONFIG_PORT_PRIORITY = """
            interface TenGigabitEthernet5/1/2
             description updated_description
             no ip address
             no ip redirects
             lacp port-priority 0
             lacp rate fast
             channel-group 3 mode active
            end""";

    private static final Config EXPECTED_CONFIG_AUTO_SPEED = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder().setAggregateId("20").build())
            .addAugmentation(LacpEthConfigAug.class,
                    new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.ACTIVE).build())
            .addAugmentation(IfEthCiscoExtAug.class,
                    new IfEthCiscoExtAugBuilder().setLacpRate(CiscoIfEthExtensionConfig.LacpRate.FAST).build())
            .build();

    private static final Config EXPECTED_PORT_PRIORITY = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder().setAggregateId("3").build())
            .addAugmentation(LacpEthConfigAug.class,
                    new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.ACTIVE).build())
            .addAugmentation(IfEthCiscoExtAug.class,
                    new IfEthCiscoExtAugBuilder().setLacpRate(CiscoIfEthExtensionConfig.LacpRate.FAST)
                            .setLacpPortPriority(0).build())
            .build();

    private static final String SH_INT_CONFIG_100_SPEED = """
            interface GigabitEthernet0/0/0
             media-type rj45
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
    void testParseGigabitEthernet() {
        var configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("GigabitEthernet0/0/0", SH_INT_CONFIG_AUTO_SPEED, configBuilder);
        assertEquals(EXPECTED_CONFIG_AUTO_SPEED, configBuilder.build());

        configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("GigabitEthernet0/0/0", SH_INT_CONFIG_100_SPEED, configBuilder);
        assertEquals(EXPECTED_CONFIG_100_SPEED, configBuilder.build());

        configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("TenGigabitEthernet5/1/2", SH_INT_CONFIG_PORT_PRIORITY, configBuilder);
        assertEquals(EXPECTED_PORT_PRIORITY, configBuilder.build());
    }

    @Test
    void testParseVlan() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("Vlan10", SH_INT_VLAN_CONFIG, configBuilder);
        assertEquals(EXPECTED_VLAN_CONFIG, configBuilder.build());
    }

}
