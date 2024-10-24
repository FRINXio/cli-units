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

package io.frinx.cli.unit.huawei.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.HuaweiIfExtensionConfig.Trust;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

class InterfaceConfigReaderTest {

    private static final String DISPLAY_CURRENT_INT = """
            #
            interface GigabitEthernet0/0/0
             speed auto
             duplex auto
             undo shutdown
             mtu 1200
             ip address 192.168.2.241 255.255.255.0
             set flow-stat interval 10
             trust dscp
            #
            return""";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("GigabitEthernet0/0/0")
            .setEnabled(true).setMtu(1200).setType(EthernetCsmacd.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setFlowStatInterval(10L)
                    .setExpireTimeout(1200L)
                    .setTrust(Trust.Dscp)
                    .setLldpEnabled(true)
                    .build()).build();

    private static final String DISPLAY_CURRENT_INT2 = """
            #
            interface LoopBack100
             description Example loopback interface
            #
            return
            """;

    private static final String DISPLAY_CURRENT_INT_OTHER_IF = """
            #
            interface Vlanif100
             description Customer access
             ip binding vpn-instance VLAN27
             ip address 172.16.90.1 255.255.255.0
             traffic-filter inbound ipv6 acl name LAN-IN
            #
            return
            """;

    private static final Config EXPECTED_CONFIG2 = new ConfigBuilder().setName("LoopBack100").setEnabled(true)
            .setDescription("Example loopback interface").setType(SoftwareLoopback.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setExpireTimeout(1200L)
                    .setLldpEnabled(true)
                    .build()).build();

    private static final Config EXPECTED_CONFIG_VLAN_IF = new ConfigBuilder()
            .setName("Vlanif100")
            .setEnabled(true)
            .setDescription("Customer access")
            .setType(L3ipvlan.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                .setIpBindingVpnInstance("VLAN27")
                    .setExpireTimeout(1200L)
                    .setLldpEnabled(true)
                .setTrafficFilter(new TrafficFilterBuilder()
                    .setDirection(Direction.Inbound)
                    .setAclName("LAN-IN")
                    .setIpv6(true)
                    .build())
                .build())
            .build();

    private static final String DISPLAY_CURRENT_INT_ARP = """
            #
            interface GigabitEthernet0/0/0
             speed auto
             duplex auto
             undo shutdown
             arp expire-time 60
             mtu 1200
             ip address 192.168.2.241 255.255.255.0
             set flow-stat interval 10
             trust 8021p inner
            #
            return""";

    private static final Config EXPECTED_CONFIG_ARP = new ConfigBuilder().setName("GigabitEthernet0/0/0")
            .setEnabled(true).setMtu(1200).setType(EthernetCsmacd.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setFlowStatInterval(10L)
                    .setTrust(Trust._8021pInner)
                    .setLldpEnabled(true)
                    .setExpireTimeout(60L)
                    .build()).build();

    @Test
    void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT, actualConfig, "GigabitEthernet0/0/0");
        assertEquals(EXPECTED_CONFIG, actualConfig.build());
    }

    @Test
    void testParseInterface2() {
        ConfigBuilder actualConfig2 = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT2, actualConfig2, "LoopBack100");
        assertEquals(EXPECTED_CONFIG2, actualConfig2.build());
    }

    @Test
    void testParseVlanifInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT_OTHER_IF, actualConfig, "Vlanif100");
        assertEquals(EXPECTED_CONFIG_VLAN_IF, actualConfig.build());
    }

    @Test
    void testParseInterfaceArp() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT_ARP, actualConfig, "GigabitEthernet0/0/0");
        assertEquals(EXPECTED_CONFIG_ARP, actualConfig.build());
    }
}