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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class InterfaceConfigWriterTest {

    private static final Config PHYSICAL_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .build();

    private static final String PHYSICAL_INT_CLEAN_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            no mtu
            no description
            shutdown
            ip redirects
            no media-type
            snmp trap link-status
            no storm-control broadcast level
            no storm-control unicast level
            lldp receive
            no negotiation auto
            no fhrp delay minimum
            no fhrp delay reload
            no hold-queue in
            no hold-queue out
            end
            """;

    private static final Config PHYSICAL_INT_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setMtu(1500)
            .setDescription("test - ethernet")
            .setEnabled(true)
            .addAugmentation(IfCiscoExtAug.class, InterfaceConfigReaderTest.IF_CISCO_EXT_AUG)
            .addAugmentation(IfSaosAug.class, InterfaceConfigReaderTest.IF_SAOS_AUG)
            .build();

    private static final String PHYSICAL_INT_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            mtu 1500
            description test - ethernet
            no shutdown
            ip redirects
            media-type rj45
            no snmp trap link-status
            storm-control broadcast level 10.00
            storm-control unicast level 10.00
            no lldp receive
            negotiation auto
            fhrp delay minimum 1
            fhrp delay reload 3600
            hold-queue 1024 in
            hold-queue 1024 out
            end
            """;

    private static final Config LOGICAL_INT_CONFIG = new ConfigBuilder()
            .setName("Loopback0")
            .setType(SoftwareLoopback.class)
            .setDescription("test - loopback")
            .setEnabled(true)
            .build();

    private static final String LOGICAL_INT_INPUT = """
            configure terminal
            interface Loopback0
            description test - loopback
            no shutdown
            ipv6 nd ra suppress all
            no ip redirects
            end
            """;

    private static final String LOGICAL_INT_DELETE_INPUT = """
            configure terminal
            no interface Loopback0
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceConfigWriter(cli);
    }

    @Test
    void updatePhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG, PHYSICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(PHYSICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    void updatePhysicalClean() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CONFIG, PHYSICAL_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(PHYSICAL_INT_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    void writeLogical() throws WriteFailedException {
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
        assertEquals(LOGICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteLogical() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, LOGICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(LOGICAL_INT_DELETE_INPUT, response.getValue().getContent());
    }

}
