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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

public class InterfaceConfigReaderTest {

    private static final String DISPLAY_CURRENT_INT = "#\n"
            + "interface GigabitEthernet0/0/0\n"
            + " speed auto\n"
            + " duplex auto\n"
            + " undo shutdown\n"
            + " mtu 1200\n"
            + " ip address 192.168.2.241 255.255.255.0\n"
            + " set flow-stat interval 10\n"
            + " trust dscp\n"
            + "#\n"
            + "return";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("GigabitEthernet0/0/0")
            .setEnabled(true).setMtu(1200).setType(EthernetCsmacd.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setFlowStatInterval(10L)
                    .setExpireTimeout(1200L)
                    .setTrustDscp(true)
                    .build()).build();

    private static final String DISPLAY_CURRENT_INT2 = "#\n"
            + "interface LoopBack100\n"
            + " description Example loopback interface\n"
            + "#\n"
            + "return\n";

    private static final String DISPLAY_CURRENT_INT_OTHER_IF = "#\n"
            + "interface Vlanif100\n"
            + " description Customer access\n"
            + " ip binding vpn-instance VLAN27\n"
            + " ip address 172.16.90.1 255.255.255.0\n"
            + " traffic-filter inbound ipv6 acl name LAN-IN\n"
            + "#\n"
            + "return\n";

    private static final Config EXPECTED_CONFIG2 = new ConfigBuilder().setName("LoopBack100").setEnabled(false)
            .setDescription("Example loopback interface").setType(SoftwareLoopback.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setExpireTimeout(1200L)
                    .build()).build();

    private static final Config EXPECTED_CONFIG_VLAN_IF = new ConfigBuilder()
            .setName("Vlanif100")
            .setEnabled(false)
            .setDescription("Customer access")
            .setType(L3ipvlan.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                .setIpBindingVpnInstance("VLAN27")
                    .setExpireTimeout(1200L)
                .setTrafficFilter(new TrafficFilterBuilder()
                    .setDirection(Direction.Inbound)
                    .setAclName("LAN-IN")
                    .setIpv6(true)
                    .build())
                .build())
            .build();

    private static final String DISPLAY_CURRENT_INT_ARP = "#\n"
            + "interface GigabitEthernet0/0/0\n"
            + " speed auto\n"
            + " duplex auto\n"
            + " undo shutdown\n"
            + " arp expire-time 60\n"
            + " mtu 1200\n"
            + " ip address 192.168.2.241 255.255.255.0\n"
            + " set flow-stat interval 10\n"
            + " trust dscp\n"
            + "#\n"
            + "return";

    private static final Config EXPECTED_CONFIG_ARP = new ConfigBuilder().setName("GigabitEthernet0/0/0")
            .setEnabled(true).setMtu(1200).setType(EthernetCsmacd.class)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setFlowStatInterval(10L)
                    .setTrustDscp(true)
                    .setExpireTimeout(60L)
                    .build()).build();

    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT, actualConfig, "GigabitEthernet0/0/0");
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());
    }

    @Test
    public void testParseInterface2() {
        ConfigBuilder actualConfig2 = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT2, actualConfig2, "LoopBack100");
        Assert.assertEquals(EXPECTED_CONFIG2, actualConfig2.build());
    }

    @Test
    public void testParseVlanifInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT_OTHER_IF, actualConfig, "Vlanif100");
        Assert.assertEquals(EXPECTED_CONFIG_VLAN_IF, actualConfig.build());
    }

    @Test
    public void testParseInterfaceArp() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(DISPLAY_CURRENT_INT_ARP, actualConfig, "GigabitEthernet0/0/0");
        Assert.assertEquals(EXPECTED_CONFIG_ARP, actualConfig.build());
    }
}
