/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.downstream.top.downstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.downstream.top.downstream.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class CableInterfaceDownstreamConfigWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface Cable1/0/3\n"
            + "downstream Downstream-Cable 1/0/0 rf-channel 0-7\n"
            + "downstream Downstream-Cable 1/0/0 rf-channel 158\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "interface Cable1/0/3\n"
            + "no downstream Downstream-Cable 1/0/0 rf-channel 158\n"
            + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface Cable1/0/3\n"
            + "no downstream Downstream-Cable 1/0/0 rf-channel 0-7\n"
            + "no downstream Downstream-Cable 1/0/0 rf-channel 158\n"
            + "downstream Downstream-Cable 1/0/0 rf-channel 158\n"
            + "exit\n"
            + "end\n";

    private static final Config WRITE_CONFIG = new ConfigBuilder()
            .setName("Downstream-Cable1/0/0")
            .setRfChannels("0-7 158")
            .build();

    private static final Config UPDATED_CONFIG = new ConfigBuilder()
            .setName("Downstream-Cable1/0/0")
            .setRfChannels("158")
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableInterfaceDownstreamConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Cable1/0/3"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableInterfaceDownstreamConfigWriter(cli);
    }

    @Test
    public void writeTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, WRITE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, WRITE_CONFIG, UPDATED_CONFIG,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, UPDATED_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}