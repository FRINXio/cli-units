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

package io.frinx.cli.unit.ios.ifc.handler;

import io.frinx.cli.io.Cli;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig.PortType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;

public class InterfaceConfigReaderTest {

    private static final Config EXPECTED_INTERFACE = new ConfigBuilder().setEnabled(false)
            .setName("GigabitEthernet1/0")
            .setType(EthernetCsmacd.class)
            .setDescription("asd fdsas'; dsa;d;fa'")
            .setMtu(1530)
            .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                    .setStormControl(Arrays.asList(new StormControlBuilder()
                            .setKey(new StormControlKey(StormControl.Address.Broadcast))
                            .setAddress(StormControl.Address.Broadcast)
                            .setLevel(new BigDecimal("50.00"))
                            .build(), new StormControlBuilder()
                            .setKey(new StormControlKey(StormControl.Address.Multicast))
                            .setAddress(StormControl.Address.Multicast)
                            .setLevel(new BigDecimal("12.34"))
                            .build()))
                    .build())
            .build();
    private static final String SH_INTERFACE_RUN = "interface GigabitEthernet1/0\n"
            + " mtu 1530\n"
            + " no ip address\n"
            + " shutdown\n"
            + " negotiation auto\n"
            + " description asd fdsas'; dsa;d;fa'\n"
            + " storm-control broadcast level 50.00\n"
            + " storm-control multicast level 12.34\n"
            + " ipv6 nd ra suppress\n"
            + "end\n";


    private static final Config EXPECTED_INTERFACE2 = new ConfigBuilder().setEnabled(true)
            .setName("FastEthernet0/0")
            .setType(EthernetCsmacd.class)
            .build();
    private static final String SH_INTERFACE_RUN2 = "interface FastEthernet0/0\n"
            + " ip address 192.168.56.121 255.255.255.0\n"
            + " duplex full\n"
            + "end\n\n";

    private static final Config EXPECTED_INTERFACE3 = new ConfigBuilder().setEnabled(true)
            .setName("GigabitEthernet0/15")
            .setType(EthernetCsmacd.class)
            .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                    .setPortType(PortType.Nni)
                    .setSwitchportPortSecurityAgingTime(5L)
                    .setL2Protocols(Arrays.asList("shutdown-threshold cdp 1000",
                            "cdp",
                            "lldp"))
                    .setLldpReceive(false)
                    .setLldpTransmit(false)
                    .setCdpEnable(false)
                    .build())
            .build();
    private static final String SH_INTERFACE_RUN3 = "interface GigabitEthernet0/15\n"
            + " port-type nni\n"
            + " switchport port-security aging time 5\n"
            + " l2protocol-tunnel shutdown-threshold cdp 1000\n"
            + " l2protocol-tunnel cdp\n"
            + " l2protocol-tunnel lldp\n"
            + " no lldp transmit\n"
            + " no lldp receive\n"
            + " no cdp enable";

    private static final Config EXPECTED_INTERFACE4 = new ConfigBuilder().setEnabled(true)
            .setName("Vlan100")
            .setType(L3ipvlan.class)
            .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                    .setVrfForwarding("VLAN011220")
                    .setIpProxyArp(false)
                    .setIpRedirects(false)
                    .setIpUnreachables(false)
                    .setSnmpTrapLinkStatus(false)
                    .build())
            .build();
    private static final String SH_INTERFACE_RUN4 = "interface Vlan100\n"
            + " ip vrf forwarding VLAN011220\n"
            + " no ip address\n"
            + " ip access-group anti-spoof-VLAN998877 in\n"
            + " no ip redirects\n"
            + " no ip unreachables\n"
            + " no ip proxy-arp\n"
            + " no snmp trap link-status\n";

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN, parsed, "GigabitEthernet1/0");
        Assert.assertEquals(EXPECTED_INTERFACE, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN2, parsed, "FastEthernet0/0");
        Assert.assertEquals(EXPECTED_INTERFACE2, parsed.build());
    }

    @Test
    public void testParseInterfaceWithL2Protocols() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN3, parsed, "GigabitEthernet0/15");
        Assert.assertEquals(EXPECTED_INTERFACE3, parsed.build());
    }

    @Test
    public void testParseVlan() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN4, parsed, "Vlan100");
        Assert.assertEquals(EXPECTED_INTERFACE4, parsed.build());
    }
}
