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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class ServiceInstanceWriterTest {

    private static final ServiceInstances WRITE_SERVICE_INSTANCES = new ServiceInstancesBuilder()
            .setServiceInstance(Arrays.asList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(false)
                                    .setEvc("EVC")
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(true)
                                    .setDot1q(Arrays.asList(1, 2, 3, 5, 6, 7, 8, 9, 10))
                                    .build())
                            .setL2protocol(new L2protocolBuilder()
                                    .setProtocol((Arrays.asList(Protocol.Lldp, Protocol.Stp)))
                                    .setProtocolType(ProtocolType.Peer)
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new ConfigBuilder()
                                    .setId(200L)
                                    .setTrunk(true)
                                    .build())
                            .build()
            ))
            .build();

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "service instance 100 ethernet EVC\n"
            + "encapsulation untagged , dot1q 1 , 2 , 3 , 5 , 6 , 7 , 8 , 9 , 10\n"
            + "l2protocol peer lldp stp\n"
            + "exit\n"
            + "service instance trunk 200 ethernet\n"
            + "exit\n"
            + "end\n";

    private static final ServiceInstances UPDATE_SERVICE_INSTANCES = new ServiceInstancesBuilder()
            .setServiceInstance(Collections.singletonList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(true)
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(false)
                                    .setDot1q(Arrays.asList(1, 9))
                                    .build())
                            .setL2protocol(new L2protocolBuilder()
                                    .setProtocol(Arrays.asList(Protocol.Lldp))
                                    .setProtocolType(ProtocolType.Peer)
                                    .build())
                            .build()
            ))
            .build();

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "no service instance 100\n"
            + "no service instance trunk 200\n"
            + "service instance trunk 100 ethernet\n"
            + "encapsulation dot1q 1 , 9\n"
            + "l2protocol peer lldp\n"
            + "exit\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "no service instance trunk 100\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ServiceInstanceWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new ServiceInstanceWriter(cli);
    }

    @Test
    public void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, getConfig(WRITE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, getConfig(WRITE_SERVICE_INSTANCES), getConfig(UPDATE_SERVICE_INSTANCES),
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, getConfig(UPDATE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    // testing service instance illegal states

    private static final IfCiscoServiceInstanceAug CLEAN_AUG = new IfCiscoServiceInstanceAugBuilder().build();

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
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(true)
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
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(true)
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(true)
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
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setDot1q(Arrays.asList(1, 2, 3))
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .build())
                            .setEncapsulation(new EncapsulationBuilder()
                                    .setUntagged(true)
                                    .setDot1q(Collections.singletonList(2))
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

        writer.updateCurrentAttributes(iid, CLEAN_AUG,
                getConfig(MORE_TRUNK_SERVICE_INSTANCES), context);
    }

    @Test
    public void testServiceInstanceWithEvcAndTrunk() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Attaching EVC to trunk service instance");

        writer.updateCurrentAttributes(iid, CLEAN_AUG,
                getConfig(TRUNK_AND_EVC_SERVICE_INSTANCE), context);
    }

    @Test
    public void testTrunkServiceInstanceWithUntaggedEncapsulation() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Untagged encapsulation in trunk service instance");

        writer.updateCurrentAttributes(iid, CLEAN_AUG,
                getConfig(UNTAGGED_ENCAPSULATION_TRUNK_SERVICE_INSTANCE), context);
    }

    @Test
    public void testMoreServiceInstanceWithUntaggedEncapsulations() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Untagged encapsulation is already configured");

        writer.updateCurrentAttributes(iid, CLEAN_AUG,
                getConfig(MORE_UNTAGGED_ENCAPSULATIONS_SERVICE_INSTANCE), context);
    }

    @Test
    public void testServiceInstanceWithSameDot1qEncapsulations() throws WriteFailedException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Some vlan ids are already configured");

        writer.updateCurrentAttributes(iid, CLEAN_AUG,
                getConfig(SAME_DOT1Q_ENCAPSULATIONS_SERVICE_INSTANCE), context);
    }

    private IfCiscoServiceInstanceAug getConfig(final ServiceInstances serviceInstances) {
        return new IfCiscoServiceInstanceAugBuilder()
                .setServiceInstances(serviceInstances)
                .build();
    }

}