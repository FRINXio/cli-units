/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure
            interface 9
            shutdown
            interface comment bla bla
            end
            """;
    private static final String UPDATE_INPUT_1 = """
            configure
            interface 44
            no shutdown
            interface comment updated desc
            mtu 1999
            speed 9999
            vxlanterminated enable
            elag 1
            elag 10
            end
            """;
    private static final String UPDATE_CLEAN_INPUT = """
            configure
            interface 44
            shutdown
            inneracl enable
            end
            """;
    private static final String DELETE_INPUT = """
            configure
            interface 44
            no shutdown
            no mtu
            rx off
            no speed
            no innerhash enable
            no inneracl enable
            no vxlanterminated enable
            no elag 5
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;

    @Test
    void write() throws WriteFailedException {
        data = null;
        data = new ConfigBuilder()
                .setName("9")
                .setDescription("bla bla")
                .setEnabled(false)
                .build();
        writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
                .setEnabled(false)
                .setName("44")
                .setType(EthernetCsmacd.class)
                .setMtu(1155)
                .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                        .setRx(true)
                        .setSpeed("auto")
                        .setElag(null)
                        .setInnerhash(true)
                        .setInneracl(true)
                        .setVxlanterminated(false)
                        .build())
                .build();
    }

    @Test
    void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().setEnabled(true).setName("44").setType(EthernetCsmacd.class)
                .setMtu(1999)
                .setDescription("updated desc")
                .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                        .setSpeed("9999")
                        .setElag(Arrays.asList((short) 1, (short) 10))
                        .setVxlanterminated(true)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT_1, response.getValue().getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().setEnabled(false).setName("44").setType(EthernetCsmacd.class)
                .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                        .setInneracl(true)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        data = new ConfigBuilder().setName("44")
                .addAugmentation(IfCubroAug.class,
                    new IfCubroAugBuilder().setElag(Lists.newArrayList((short)5)).build())
                .setType(EthernetCsmacd.class).build();

        writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
