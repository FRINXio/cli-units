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

package io.frinx.cli.unit.iosxe.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.hold.queue.HoldQueueBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Bridge;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final List<StormControl> STORM_CONTROL_LIST = Arrays.asList(
            new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Broadcast))
                    .setAddress(StormControl.Address.Broadcast)
                    .setLevel(new BigDecimal("10.00"))
                    .build(),
            new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Unicast))
                    .setAddress(StormControl.Address.Unicast)
                    .setLevel(new BigDecimal("10.00"))
                    .build()
    );

    public static final IfCiscoExtAug IF_CISCO_EXT_AUG = new IfCiscoExtAugBuilder()
            .setFhrpMinimumDelay(1)
            .setFhrpReloadDelay(3600)
            .setStormControl(STORM_CONTROL_LIST)
            .setSnmpTrapLinkStatus(false)
            .setLldpReceive(false)
            .setNegotiationAuto(true)
            .setIpRedirects(true)
            .setHoldQueue(new HoldQueueBuilder()
                    .setIn(1024L)
                    .setOut(1024L)
                    .build()
            )
            .build();

    public static final IfCiscoExtAug IF_CISCO_EXT_AUG_WITH_VRF = new IfCiscoExtAugBuilder()
            .setIpv6NdRaSuppress("all")
            .setVrfForwarding("VLAN123456")
            .setIpProxyArp(false)
            .setIpRedirects(false)
            .build();

    public static final IfSaosAug IF_SAOS_AUG = new IfSaosAugBuilder()
            .setPhysicalType(SaosIfExtensionConfig.PhysicalType.Rj45)
            .build();

    private static final Config EXPECTED_INTERFACE = new ConfigBuilder()
            .setEnabled(false)
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setDescription("test - description")
            .setMtu(1530)
            .addAugmentation(IfCiscoExtAug.class, IF_CISCO_EXT_AUG)
            .addAugmentation(IfSaosAug.class, IF_SAOS_AUG)
            .build();

    private static final Config EXPECTED_INTERFACE_WITH_VRF = new ConfigBuilder()
            .setEnabled(true)
            .setName("BDI101")
            .setType(Bridge.class)
            .setDescription("LAN VLAN123456")
            .addAugmentation(IfCiscoExtAug.class, IF_CISCO_EXT_AUG_WITH_VRF)
            .build();

    private static final String SH_INTERFACE_RUN = """
            interface GigabitEthernet0/0/0
             mtu 1530
             no ip address
             shutdown
             negotiation auto
             description test - description
             fhrp delay minimum 1
             fhrp delay reload 3600
             no lldp receive
             negotiation info
             media-type rj45
             no snmp trap link-status
             storm-control broadcast level 10.00
             storm-control multicast level bps 0
             storm-control unicast level 10.00
             hold-queue 1024 in
             hold-queue 1024 out
             !
            end
            """;

    private static final String SH_INTERFACE_BDI_RUN = """
            interface BDI101
             description LAN VLAN123456
             vrf forwarding VLAN123456
             ip address 120.130.140.1 255.255.255.248
             no ip redirects
             no ip proxy-arp
             ip access-group ACL_CPE_PROT_VLAN123456_LAN_IN_V4 in
             ipv6 address 2001:ABC:ABC::1/64
             ipv6 nd ra suppress all
             ipv6 traffic-filter ACL_CPE_PROT_VLAN123456_LAN_IN_V6 in
            """;

    @Test
    void testParseInterface() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN, configBuilder, "GigabitEthernet0/0/0");
        assertEquals(EXPECTED_INTERFACE, configBuilder.build());
    }

    @Test
    void testParseInterfaceWithVRF() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_BDI_RUN, configBuilder, "BDI101");
        assertEquals(EXPECTED_INTERFACE_WITH_VRF, configBuilder.build());
    }

}
