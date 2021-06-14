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

package io.frinx.cli.unit.ios.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

    private static final String PHYSICAL_INTERFACE_WRITE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/1\n"
            + "no shutdown\n"
            + "port-type uni\n"
            + "switchport mode dot1q-tunnel\n"
            + "snmp trap link-status\n"
            + "storm-control broadcast level 10.00\n"
            + "end\n"
            + "\n";

    private static final String PHYSICAL_INTERFACE_UPDATE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/1\n"
            + "no shutdown\n"
            + "no port-type\n"
            + "no switchport mode\n"
            + "snmp trap link-status\n"
            + "storm-control broadcast level 11.00\n"
            + "storm-control unicast level 67.89\n"
            + "lldp transmit\n"
            + "lldp receive\n"
            + "end\n"
            + "\n";

    private static final String PHYSICAL_INTERFACE_CLEAN_UPDATE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/1\n"
            + "shutdown\n"
            + "no port-type\n"
            + "no switchport mode\n"
            + "snmp trap link-status\n"
            + "no storm-control broadcast level\n"
            + "no storm-control unicast level\n"
            + "no lldp receive\n"
            + "end\n"
            + "\n";

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 35\n"
            + "description test desc\n"
            + "no shutdown\n"
            + "no ip redirects\n"
            + "ipv6 nd ra suppress all\n"
            + "vrf forwarding vlan300\n"
            + "end\n"
            + "\n";

    private static final String WRITE_EMPTY_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "shutdown\n"
            + "end\n"
            + "\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 50\n"
            + "description updated desc\n"
            + "shutdown\n"
            + "no snmp trap link-status\n"
            + "no ip redirects\n"
            + "no ip unreachables\n"
            + "no ip proxy-arp\n"
            + "end\n"
            + "\n";

    private static final String UPDATE_CLEAN_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "no mtu\n"
            + "no description\n"
            + "shutdown\n"
            + "end\n"
            + "\n";

    private static final String UPDATE_SOME_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 30\n"
            + "shutdown\n"
            + "end\n"
            + "\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "no interface Bundle-Ether45\n"
            + "end\n";

    private static final String L2_PROTOCOL_BASE = "configure terminal\n"
            + "interface GigabitEthernet0/1\n"
            + "no shutdown\n"
            + "no port-type\n"
            + "no switchport mode\n"
            + "snmp trap link-status\n";

    private static final String L2_PROTOCOL_WRITE_INPUT = L2_PROTOCOL_BASE
            + "l2protocol-tunnel shutdown-threshold cdp 1000\n"
            + "l2protocol-tunnel cdp\n"
            + "end\n"
            + "\n";

    private static final String L2_PROTOCOL_UPDATE_INPUT = L2_PROTOCOL_BASE
            + "no l2protocol-tunnel shutdown-threshold cdp 1000\n"
            + "l2protocol-tunnel stp\n"
            + "lldp transmit\n"
            + "lldp receive\n"
            + "cdp enable\n"
            + "end\n"
            + "\n";

    private static final String L2_PROTOCOL_DELETE_INPUT = L2_PROTOCOL_BASE
            + "no l2protocol-tunnel shutdown-threshold cdp 1000\n"
            + "no l2protocol-tunnel cdp\n"
            + "end\n"
            + "\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;
    private Config l2ProtocolData0;
    private Config l2ProtocolData1;
    private Config l2ProtocolData2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setEnabled(true).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(35).setDescription("test desc")
                .build();

        l2ProtocolData0 = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .build();

        IfCiscoExtAugBuilder ifCiscoExtAugBuilder1 = new IfCiscoExtAugBuilder()
                .setL2Protocols(Arrays.asList("shutdown-threshold cdp 1000",
                        "cdp"))
                .setLldpReceive(false)
                .setLldpTransmit(false)
                .setCdpEnable(false);

        l2ProtocolData1 = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder1.build())
                .build();

        IfCiscoExtAugBuilder ifCiscoExtAugBuilder2 = new IfCiscoExtAugBuilder()
                .setL2Protocols(Arrays.asList("stp",
                        "cdp"))
                .setLldpReceive(true)
                .setLldpTransmit(true)
                .setCdpEnable(true);

        l2ProtocolData2 = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder2.build())
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        ifCiscoExtAugBuilder.setIpRedirects(false);
        ifCiscoExtAugBuilder.setIpv6NdRaSuppress("all");
        ifCiscoExtAugBuilder.setVrfForwarding("vlan300");

        // write values
        Config newData = new ConfigBuilder().setEnabled(true).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(35).setDescription("test desc")
                .addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build())
                .build();
        this.writer.writeCurrentAttributes(iid, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeEmpty() throws WriteFailedException {
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .build();

        this.writer.writeCurrentAttributes(iid, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_EMPTY_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        ifCiscoExtAugBuilder.setIpProxyArp(false);
        ifCiscoExtAugBuilder.setIpRedirects(false);
        ifCiscoExtAugBuilder.setIpUnreachables(false);
        ifCiscoExtAugBuilder.setSnmpTrapLinkStatus(false);

        // update values
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(50).setDescription("updated desc")
                .addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateSome() throws WriteFailedException {
        // mtu has changed, description has not
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(30).setDescription("test desc")
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_SOME_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writePhysicalInterface() throws WriteFailedException {
        // because physical interface can't be created, so it needs to be UPDATE
        Config dataBefore = new ConfigBuilder()
                .setEnabled(false)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .build();

        data = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                        .setPortType(CiscoIfExtensionConfig.PortType.Uni)
                        .setSwitchportMode(CiscoIfExtensionConfig.SwitchportMode.Dot1qTunnel)
                        .setStormControl(getStormControls("10.00", null, null))
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, dataBefore, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(PHYSICAL_INTERFACE_WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updatePhysicalInterface() throws WriteFailedException {
        Config dataBefore = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                        .setStormControl(getStormControls("10.00", null, null))
                        .build())
                .build();

        data = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                        .setLldpTransmit(Boolean.TRUE)
                        .setLldpReceive(Boolean.TRUE)
                        .setStormControl(getStormControls("11.00", null, "67.89"))
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, dataBefore, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(PHYSICAL_INTERFACE_UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void cleanUpdatePhysicalInterface() throws WriteFailedException {
        Config dataBefore = new ConfigBuilder()
                .setEnabled(true)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                        .setLldpReceive(Boolean.TRUE)
                        .setStormControl(getStormControls("11.00", null, "67.89"))
                        .build())
                .build();

        data = new ConfigBuilder()
                .setEnabled(false)
                .setName("GigabitEthernet0/1")
                .setType(EthernetCsmacd.class)
                .build();

        this.writer.updateCurrentAttributes(iid, dataBefore, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(PHYSICAL_INTERFACE_CLEAN_UPDATE_INPUT, response.getValue().getContent());
    }

    private List<StormControl> getStormControls(String broadcastValue, String multicastValue, String unicastValue) {
        List<StormControl> stormControls = new ArrayList<>();
        if (broadcastValue != null) {
            stormControls.add(new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Broadcast))
                    .setAddress(StormControl.Address.Broadcast)
                    .setLevel(new BigDecimal(broadcastValue))
                    .build());
        }
        if (multicastValue != null) {
            stormControls.add(new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Multicast))
                    .setAddress(StormControl.Address.Multicast)
                    .setLevel(new BigDecimal(multicastValue))
                    .build());
        }
        if (unicastValue != null) {
            stormControls.add(new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Unicast))
                    .setAddress(StormControl.Address.Unicast)
                    .setLevel(new BigDecimal(unicastValue))
                    .build());
        }
        return stormControls;
    }

    @Test
    public void l2ProtocolWrite() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, l2ProtocolData0, l2ProtocolData1, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(L2_PROTOCOL_WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void l2ProtocolUpdate() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, l2ProtocolData1, l2ProtocolData2, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(L2_PROTOCOL_UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void l2ProtocolDelete() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, l2ProtocolData1, l2ProtocolData0, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(L2_PROTOCOL_DELETE_INPUT, response.getValue().getContent());
    }
}
