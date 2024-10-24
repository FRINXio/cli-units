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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top.IfRpdDsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.DownstreamPortsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.us.top.IfRpdUsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.us.top._if.rpd.us.UpstreamPortsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.Rpds;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.CoreInterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.CoreInterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.core._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableRpdCoreInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            core-interface Te1/1/0
            principal
            network-delay dlm 1
            rpd-ds 0 Downstream-Cable 1/0/16 profile 3
            rpd-us 0 Upstream-Cable 1/0/16 profile 4
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            core-interface Te1/1/0
            no principal
            no rpd-ds 0 Downstream-Cable 1/0/16
            rpd-ds 0 Downstream-Cable 1/0/18 profile 4
            no rpd-us 0 Upstream-Cable 1/0/16
            rpd-us 0 Upstream-Cable 1/0/17 profile 5
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            no core-interface Te1/1/0
            end
            """;

    private static final CoreInterface CONFIG_WRITE = new CoreInterfaceBuilder()
            .setConfig(new ConfigBuilder()
                    .setName("Te1/1/0")
                    .setNetworkDelay("dlm 1")
                    .setPrincipal(Boolean.TRUE)
                    .build())
            .setIfRpdDs(new IfRpdDsBuilder()
                    .setDownstreamPorts(Lists.newArrayList(new DownstreamPortsBuilder()
                            .setId("0")
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .cable.rev211102._if.rpd.ds.top._if.rpd.ds.downstream.ports
                                    .ConfigBuilder()
                                    .setCableController("Downstream-Cable1/0/16")
                                    .setProfile("3")
                                    .build())
                            .build()))
                    .build())
            .setIfRpdUs(new IfRpdUsBuilder()
                    .setUpstreamPorts(Lists.newArrayList(new UpstreamPortsBuilder()
                            .setId("0")
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .cable.rev211102._if.rpd.us.top._if.rpd.us.upstream.ports
                                    .ConfigBuilder()
                                    .setCableController("Upstream-Cable1/0/16")
                                    .setProfile("4")
                                    .build())
                            .build()))
                    .build())
            .build();

    private static final CoreInterface CONFIG_UPDATE = new CoreInterfaceBuilder()
            .setConfig(new ConfigBuilder()
                    .setName("Te1/1/0")
                    .setPrincipal(Boolean.FALSE)
                    .build())
            .setIfRpdDs(new IfRpdDsBuilder()
                    .setDownstreamPorts(Lists.newArrayList(new DownstreamPortsBuilder()
                            .setId("0")
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .cable.rev211102._if.rpd.ds.top._if.rpd.ds.downstream.ports
                                    .ConfigBuilder()
                                    .setCableController("Downstream-Cable1/0/18")
                                    .setProfile("4")
                                    .build())
                            .build()))
                    .build())
            .setIfRpdUs(new IfRpdUsBuilder()
                    .setUpstreamPorts(Lists.newArrayList(new UpstreamPortsBuilder()
                            .setId("0")
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .cable.rev211102._if.rpd.us.top._if.rpd.us.upstream.ports
                                    .ConfigBuilder()
                                    .setCableController("Upstream-Cable1/0/17")
                                    .setProfile("5")
                                    .build())
                            .build()))
                    .build())
            .build();

    private static final CoreInterface CONFIG_DELETE = new CoreInterfaceBuilder()
            .setConfig(new ConfigBuilder()
                    .setName("Te1/1/0")
                    .build())
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableRpdCoreInterfaceConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Rpds.class)
            .child(Rpd.class, new RpdKey("VFZ-RPD-100"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableRpdCoreInterfaceConfigWriter(cli);
    }

    @Test
    void writeTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, CONFIG_WRITE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, CONFIG_WRITE, CONFIG_UPDATE,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, CONFIG_DELETE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
