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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class ServiceInstanceWriterTest {

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
                                .setProtocol(Arrays.asList(Protocol.RB, Protocol.RC, Protocol.RD,
                                        Protocol.RF, Protocol.Mmrp, Protocol.Mvrp))
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
            .build(),
        new ServiceInstanceBuilder()
            .setId(300L)
            .setKey(new ServiceInstanceKey(300L))
            .setConfig(new ConfigBuilder()
                .setId(300L)
                .setTrunk(true)
                .build())
            .setEncapsulation(new EncapsulationBuilder()
                .setDot1q(Collections.singletonList("200"))
                .build())
            .setBridgeDomain(new BridgeDomainBuilder()
                .setValue("from-encapsulation")
                .build())
            .build());

    private static final String WRITE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            service instance 100 ethernet EVC
            encapsulation untagged , dot1q 1-10, 15
            l2protocol Peer RB RC RD RF Mmrp Mvrp
            bridge-domain 100 split-horizon group 2
            rewrite Ingress tag Pop 1 symmetric
            exit
            service instance trunk 200 ethernet
            encapsulation dot1q 200
            bridge-domain from-encapsulation
            exit
            service instance trunk 300 ethernet
            encapsulation dot1q 200
            bridge-domain from-encapsulation
            exit
            end
            """;

    private static final List<ServiceInstance> UPDATE_SERVICE_INSTANCES = Arrays.asList(
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
            .build(),
            new ServiceInstanceBuilder()
                    .setId(500L)
                    .setKey(new ServiceInstanceKey(500L))
                    .setConfig(new ConfigBuilder()
                            .setId(500L)
                            .setTrunk(false)
                            .build())
                    .build());

    private static final String UPDATE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            no service instance trunk 200
            no service instance trunk 300
            no service instance trunk 100
            no service instance 500
            service instance trunk 100 ethernet
            encapsulation dot1q 1, 9
            l2protocol Peer RB Lacp
            exit
            service instance 500 ethernet
            exit
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            no service instance trunk 100
            no service instance 500
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ServiceInstanceWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new ServiceInstanceWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, getAug(WRITE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, getAug(WRITE_SERVICE_INSTANCES), getAug(UPDATE_SERVICE_INSTANCES),
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, getAug(UPDATE_SERVICE_INSTANCES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private IfCiscoServiceInstanceAug getAug(final List<ServiceInstance> serviceInstances) {
        return new IfCiscoServiceInstanceAugBuilder()
                .setServiceInstances(new ServiceInstancesBuilder()
                    .setServiceInstance(serviceInstances)
                    .build())
                .build();
    }

}
