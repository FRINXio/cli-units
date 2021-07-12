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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

    private static final Config PHYSICAL_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .build();

    private static final String PHYSICAL_INT_CLEAN_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "no mtu\n"
            + "no description\n"
            + "shutdown\n"
            + "no media-type\n"
            + "snmp trap link-status\n"
            + "no storm-control broadcast level\n"
            + "no storm-control unicast level\n"
            + "lldp receive\n"
            + "no fhrp delay minimum\n"
            + "no fhrp delay reload\n"
            + "end\n";

    private static final Config PHYSICAL_INT_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setMtu(1500)
            .setDescription("test - ethernet")
            .setEnabled(true)
            .addAugmentation(IfCiscoExtAug.class, InterfaceConfigReaderTest.IF_CISCO_EXT_AUG)
            .addAugmentation(IfSaosAug.class, InterfaceConfigReaderTest.IF_SAOS_AUG)
            .build();

    private static final String PHYSICAL_INT_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "mtu 1500\n"
            + "description test - ethernet\n"
            + "no shutdown\n"
            + "media-type rj45\n"
            + "no snmp trap link-status\n"
            + "storm-control broadcast level 10.00\n"
            + "storm-control unicast level 10.00\n"
            + "no lldp receive\n"
            + "fhrp delay minimum 1\n"
            + "fhrp delay reload 3600\n"
            + "end\n";

    private static final Config LOGICAL_INT_CONFIG = new ConfigBuilder()
            .setName("Loopback0")
            .setType(SoftwareLoopback.class)
            .setDescription("test - loopback")
            .setEnabled(true)
            .build();

    private static final String LOGICAL_INT_INPUT = "configure terminal\n"
            + "interface Loopback0\n"
            + "no mtu\n"
            + "description test - loopback\n"
            + "no shutdown\n"
            + "snmp trap link-status\n"
            + "ipv6 nd ra suppress all\n"
            + "no ip redirects\n"
            + "end\n";

    private static final String LOGICAL_INT_DELETE_INPUT = "configure terminal\n"
            + "no interface Loopback0\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceConfigWriter(cli);
    }

    @Test
    public void updatePhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG, PHYSICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(PHYSICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    public void updatePhysicalClean() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CONFIG, PHYSICAL_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(PHYSICAL_INT_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeLogical() throws WriteFailedException {
        IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        ifCiscoExtAugBuilder.setIpRedirects(false);
        ifCiscoExtAugBuilder.setIpv6NdRaSuppress("all");

        // write values
        Config newData = new ConfigBuilder().setEnabled(true).setName("Loopback0").setType(Ieee8023adLag.class)
                .setDescription("test - loopback")
                .addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build())
                .build();
        writer.writeCurrentAttributes(iid, newData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(LOGICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    public void deleteLogical() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, LOGICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(LOGICAL_INT_DELETE_INPUT, response.getValue().getContent());
    }

}
