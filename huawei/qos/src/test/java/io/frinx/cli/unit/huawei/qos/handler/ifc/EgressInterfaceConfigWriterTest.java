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
package io.frinx.cli.unit.huawei.qos.handler.ifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosEgressInterfaceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosEgressInterfaceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.output.top.output.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.output.top.output.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class EgressInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = """
            system-view
            interface GigabitEthernet0/0/4.99
            traffic-policy TP-NNI-MAIN-OUT outbound
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            interface GigabitEthernet0/0/4.99
            undo traffic-policy outbound
            return
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EgressInterfaceConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/4.99"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new EgressInterfaceConfigWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, getConfig("TP-NNI-MAIN-OUT"), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, getConfig("TP-NNI-MAIN-OUT"), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private Config getConfig(final String servicePolicy) {
        return new ConfigBuilder()
                .addAugmentation(QosEgressInterfaceAug.class,
                        new QosEgressInterfaceAugBuilder()
                                .setServicePolicy(servicePolicy)
                                .build())
                .build();
    }
}
