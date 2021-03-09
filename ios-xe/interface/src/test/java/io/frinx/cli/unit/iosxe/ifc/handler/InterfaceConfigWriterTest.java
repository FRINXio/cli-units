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
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
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
            + "no storm-control broadcast level\n"
            + "no storm-control unicast level\n"
            + "lldp transmit\n"
            + "lldp receive\n"
            + "no service instance 100\n"
            + "no service instance trunk 200\n"
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
            + "storm-control broadcast level 10.00\n"
            + "storm-control unicast level 10.00\n"
            + "no lldp transmit\n"
            + "no lldp receive\n"
            + "service instance 100 ethernet EVC\n"
            + "encapsulation untagged , dot1q 1 , 2 , 3 , 5 , 6 , 7 , 8 , 9 , 10\n"
            + "exit\n"
            + "service instance trunk 200 ethernet\n"
            + "exit\n"
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
        writer.writeCurrentAttributes(iid, LOGICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(LOGICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    public void deleteLogical() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, LOGICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(LOGICAL_INT_DELETE_INPUT, response.getValue().getContent());
    }

    // testing service instance illegal states

    private static final ServiceInstances MORE_TRUNK_SERVICE_INSTANCES = new ServiceInstancesBuilder()
            .setServiceInstance(Arrays.asList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(true)
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .setTrunk(true)
                                    .build())
                            .build()
            ))
            .build();

    private static final ServiceInstances TRUNK_AND_EVC_SERVICE_INSTANCE = new ServiceInstancesBuilder()
            .setServiceInstance(Collections.singletonList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(true)
                                    .setEvc("EVC")
                                    .build())
                            .build()
            ))
            .build();

    private static final ServiceInstances UNTAGGED_ENCAPSULATION_TRUNK_SERVICE_INSTANCE = new ServiceInstancesBuilder()
            .setServiceInstance(Collections.singletonList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(true)
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setUntagged(true)
                                            .build())
                                    .build())
                            .build()
            ))
            .build();

    private static final ServiceInstances MORE_UNTAGGED_ENCAPSULATIONS_SERVICE_INSTANCE = new ServiceInstancesBuilder()
            .setServiceInstance(Arrays.asList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setUntagged(true)
                                            .build())
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setUntagged(true)
                                            .build())
                                    .build())
                            .build()
            ))
            .build();

    private static final ServiceInstances SAME_DOT1Q_ENCAPSULATIONS_SERVICE_INSTANCE = new ServiceInstancesBuilder()
            .setServiceInstance(Arrays.asList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(true)
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setDot1q(Arrays.asList(1, 2, 3))
                                            .build())
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setUntagged(true)
                                            .setDot1q(Collections.singletonList(2))
                                            .build())
                                    .build())
                            .build()
            ))
            .build();


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testServiceInstanceWithMoreTrunks() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Only one trunk service instance");

        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG,
                getConfig(MORE_TRUNK_SERVICE_INSTANCES), context);
    }

    @Test
    public void testServiceInstanceWithEvcAndTrunk() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Attaching EVC to trunk service instance");

        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG,
                getConfig(TRUNK_AND_EVC_SERVICE_INSTANCE), context);
    }

    @Test
    public void testTrunkServiceInstanceWithUntaggedEncapsulation() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Untagged encapsulation in trunk service instance");

        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG,
                getConfig(UNTAGGED_ENCAPSULATION_TRUNK_SERVICE_INSTANCE), context);
    }

    @Test
    public void testMoreServiceInstanceWithUntaggedEncapsulations() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Untagged encapsulation is already configured");

        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG,
                getConfig(MORE_UNTAGGED_ENCAPSULATIONS_SERVICE_INSTANCE), context);
    }

    @Test
    public void testServiceInstanceWithSameDot1qEncapsulations() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Some vlan ids are already configured");

        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG,
                getConfig(SAME_DOT1Q_ENCAPSULATIONS_SERVICE_INSTANCE), context);
    }

    private Config getConfig(final ServiceInstances serviceInstances) {
        return new ConfigBuilder()
                .setName("GigabitEthernet0/0/0")
                .setType(EthernetCsmacd.class)
                .setEnabled(true)
                .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder()
                        .setServiceInstances(serviceInstances)
                        .build())
                .build();
    }

}