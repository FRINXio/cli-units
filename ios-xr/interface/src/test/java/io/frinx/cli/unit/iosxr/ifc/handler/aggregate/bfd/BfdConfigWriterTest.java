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

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class BfdConfigWriterTest {

    private static final String WRITE_INPUT = """
            interface Bundle-Ether55
            bfd mode ietf
            bfd address-family ipv4 fast-detect
            bfd address-family ipv4 multiplier 5
            bfd address-family ipv4 minimum-interval 1000
            bfd address-family ipv4 destination 192.168.1.1
            root
            """;

    private static final String UPDATE_INPUT = """
            interface Bundle-Ether55
            bfd mode ietf
            bfd address-family ipv4 fast-detect
            bfd address-family ipv4 multiplier 10
            bfd address-family ipv4 minimum-interval 2000
            bfd address-family ipv4 destination 192.168.1.2
            root
            """;

    private static final String UPDATE_CLEAN_INPUT = """
            interface Bundle-Ether55
            bfd mode ietf
            bfd address-family ipv4 fast-detect
            no bfd address-family ipv4 multiplier
            no bfd address-family ipv4 minimum-interval
            no bfd address-family ipv4 destination
            root
            """;

    private static final String DELETE_INPUT = """
            interface Bundle-Ether55
            no bfd mode ietf
            no bfd address-family ipv4 fast-detect
            no bfd address-family ipv4 multiplier
            no bfd address-family ipv4 minimum-interval
            no bfd address-family ipv4 destination
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private BfdConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Bundle-Ether55"));

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new BfdConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setDestinationAddress(new Ipv4Address("192.168.1.1"))
                .setMinInterval(1000L)
                .setMultiplier(5L)
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
        // update values
        Config newData = new ConfigBuilder().setDestinationAddress(new Ipv4Address("192.168.1.2"))
                .setMinInterval(2000L)
                .setMultiplier(10L)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
