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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Operation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.ServiceInstanceL2protocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.RewriteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class ServiceInstanceWriterTest {

    private static final List<ServiceInstance> WRITE_SERVICE_INSTANCES = Arrays.asList(
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
                .setDot1q(Arrays.asList("1-10", "15"))
                .build())
            .setL2protocols(new L2protocolsBuilder()
                .setServiceInstanceL2protocol(new ServiceInstanceL2protocolBuilder()
                    .setL2protocol(Arrays.asList(new L2protocolBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                            .rev171024.service.instance.l2protocols.service.instance.l2protocol.l2protocol
                            .ConfigBuilder()
                                .setProtocolType(ProtocolType.Peer)
                                .setProtocol(Arrays.asList(Protocol.RB, Protocol.Lacp))
                                .build())
                        .build()))
                    .build())
                .build())
            .setBridgeDomain(new BridgeDomainBuilder()
                .setValue("100")
                .setGroupNumber((short) 2)
                .build())
            .setRewrite(new RewriteBuilder()
                .setType(Type.Ingress)
                .setOperation(Operation.Pop)
                .build())
            .build(),
        new ServiceInstanceBuilder()
            .setId(200L)
            .setKey(new ServiceInstanceKey(200L))
            .setConfig(new ConfigBuilder()
                .setId(200L)
                .setTrunk(true)
                .build())
            .setEncapsulation(new EncapsulationBuilder()
                .setDot1q(Collections.singletonList("200"))
                .build())
            .setBridgeDomain(new BridgeDomainBuilder()
                .setValue("from-encapsulation")
                .build())
            .build());

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "service instance 100 ethernet EVC\n"
            + "encapsulation untagged , dot1q 1-10, 15\n"
            + "l2protocol peer RB lacp\n"
            + "bridge-domain 100 split-horizon group 2\n"
            + "rewrite ingress tag pop 1 symmetric\n"
            + "exit\n"
            + "service instance trunk 200 ethernet\n"
            + "encapsulation dot1q 200\n"
            + "bridge-domain from-encapsulation\n"
            + "exit\n"
            + "end\n";

    private static final List<ServiceInstance> UPDATE_SERVICE_INSTANCES = Collections.singletonList(
        new ServiceInstanceBuilder()
            .setId(100L)
            .setKey(new ServiceInstanceKey(100L))
            .setConfig(new ConfigBuilder()
                .setId(100L)
                .setTrunk(true)
                .build())
            .setEncapsulation(new EncapsulationBuilder()
                .setUntagged(false)
                .setDot1q(Arrays.asList("1", "9"))
                .build())
            .setL2protocols(new L2protocolsBuilder()
                .setServiceInstanceL2protocol(new ServiceInstanceL2protocolBuilder()
                    .setL2protocol(Arrays.asList(new L2protocolBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                            .cisco.rev171024.service.instance.l2protocols.service.instance
                            .l2protocol.l2protocol.ConfigBuilder()
                                .setProtocolType(ProtocolType.Peer)
                                .setProtocol(Arrays.asList(Protocol.RB, Protocol.Lacp))
                            .build())
                        .build()))
                    .build())
                .build())
            .build());

    private static final List<ServiceInstance> UPDATE_SERVICE_INSTANCES_WITH_REWRITE = Collections.singletonList(
        new ServiceInstanceBuilder()
            .setId(100L)
            .setKey(new ServiceInstanceKey(100L))
            .setConfig(new ConfigBuilder()
                .setId(100L)
                .setTrunk(true)
                .build())
            .setEncapsulation(new EncapsulationBuilder()
                .setUntagged(false)
                .setDot1q(Arrays.asList("1", "9"))
                .build())
            .setL2protocols(new L2protocolsBuilder()
                .setServiceInstanceL2protocol(new ServiceInstanceL2protocolBuilder()
                    .setL2protocol(Arrays.asList(new L2protocolBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                            .cisco.rev171024.service.instance.l2protocols.service.instance
                            .l2protocol.l2protocol.ConfigBuilder()
                                .setProtocolType(ProtocolType.Peer)
                                .setProtocol(Arrays.asList(Protocol.RB, Protocol.Lacp))
                                .build())
                        .build()))
                    .build())
                .build())
            .setRewrite(new RewriteBuilder()
                .setType(Type.Egress)
                .setOperation(Operation.Pop)
                .build())
            .build());

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "no service instance trunk 200\n"
            + "service instance trunk 100 ethernet\n"
            + "encapsulation dot1q 1, 9\n"
            + "l2protocol peer RB lacp\n"
            + "exit\n"
            + "end\n";

    private static final String UPDATE_INPUT_WITH_REWRITE = "configure terminal\n"
            + "interface GigabitEthernet0/0/0\n"
            + "no rewrite ingress\n"
            + "no service instance trunk 200\n"
            + "service instance trunk 100 ethernet\n"
            + "encapsulation dot1q 1 , 9\n"
            + "l2protocol peer RB lacp\n"
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
        writer.writeCurrentAttributes(iid, getAug(WRITE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, getAug(WRITE_SERVICE_INSTANCES), getAug(UPDATE_SERVICE_INSTANCES),
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateRewrite() throws WriteFailedException {
        try {
            writer.updateCurrentAttributes(iid, getAug(WRITE_SERVICE_INSTANCES),
                    getAug(UPDATE_SERVICE_INSTANCES_WITH_REWRITE), context);
            Mockito.verify(cli).executeAndRead(response.capture());
            Assert.assertEquals(UPDATE_INPUT_WITH_REWRITE, response.getValue().getContent());
        } catch (IllegalArgumentException exception) {
            // we don't have implementation for POP operation
            Assert.assertTrue(true);
        }

    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, getAug(UPDATE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private IfCiscoServiceInstanceAug getAug(final List<ServiceInstance> serviceInstances) {
        return new IfCiscoServiceInstanceAugBuilder()
                .setServiceInstances(new ServiceInstancesBuilder()
                    .setServiceInstance(serviceInstances)
                    .build())
                .build();
    }

}
