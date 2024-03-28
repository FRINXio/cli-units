/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConnBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.top.rpd.ds.conn.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.top.rpd.ds.conn.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class InterfaceRpdDsConnConfigWriterTest {

    private static final Config WRITE_CONFIG = new ConfigBuilder()
            .setDsConn(Arrays.asList(
                    new DsConnBuilder()
                            .setId(0)
                            .setPowerLevel(320)
                            .setDsGroup(Collections.singletonList("\"SG_2827355189\""))
                            .build()))
            .build();

    private static final String WRITE_INPUT = """
            configure interface rpd "MND-GT0002-RPD1"
            ds-conn 0 power-level 320
            ds-conn 0 ds-group "SG_2827355189"
            end
            """;

    private static final Config UPDATE_CONFIG = new ConfigBuilder()
            .setDsConn(Collections.singletonList(
                    new DsConnBuilder()
                            .setId(0)
                            .setPowerLevel(360)
                            .setDsGroup(Collections.singletonList("\"SG_2827355187\""))
                            .build()))
            .build();

    private static final String UPDATE_INPUT = """
            configure interface rpd "MND-GT0002-RPD1"
            ds-conn 0 no power-level 320
            ds-conn 0 no ds-group "SG_2827355189"
            ds-conn 0 power-level 360
            ds-conn 0 ds-group "SG_2827355187"
            end
            """;

    private static final String DELETE_INPUT = """
            configure interface rpd "MND-GT0002-RPD1"
            ds-conn 0 no power-level 320
            ds-conn 0 no ds-group "SG_2827355189"
            end
            """;

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private InterfaceRpdDsConnConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("rpd \"MND-GT0002-RPD1\""));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceRpdDsConnConfigWriter(cli);
    }

    @Test
    void testWrite() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, WRITE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdate() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, WRITE_CONFIG, UPDATE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDelete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, WRITE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
