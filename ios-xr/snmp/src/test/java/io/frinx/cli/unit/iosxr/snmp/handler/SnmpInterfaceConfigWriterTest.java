/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.snmp.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEventBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class SnmpInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = """
            snmp-server interface Loopback0
            ?
            no notification linkupdown disable
            root
            """;

    private static final String DELETE_INPUT = "no snmp-server interface Loopback0\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier<Config> iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("Loopback0")))
            .child(Config.class);

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
                .setInterfaceId(iid.firstKeyOf(Interface.class).getInterfaceId())
                .setEnabledTrapForEvent(
                        Lists.newArrayList(new EnabledTrapForEventBuilder()
                                .setEventName(LINKUPDOWN.class)
                                .setEnabled(true)
                                .build()))
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, data, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());

        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void writeWrongData() throws WriteFailedException {
        Config wrongData = new ConfigBuilder().setEnabledTrapForEvent(
                Lists.newArrayList(new EnabledTrapForEventBuilder().build()))
                .build();

        this.writer.writeCurrentAttributes(iid, wrongData, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(response.capture());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
