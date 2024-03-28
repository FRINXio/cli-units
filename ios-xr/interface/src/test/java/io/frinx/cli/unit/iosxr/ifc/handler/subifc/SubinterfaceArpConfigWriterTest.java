/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class SubinterfaceArpConfigWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private SubinterfaceArpConfigWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String WRITE_INPUT = """
            interface Bundle-Ether1000.200
            arp timeout 60
            root
            """;
    private static final String DELETE_INPUT = """
            interface Bundle-Ether1000.200
            no arp timeout
            root
            """;


    private static final String INTERFACE_NAME = "Bundle-Ether1000";
    private static final Long SUBIFC_INDEX = Long.valueOf(200L);

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);
    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(
        IIDs.IN_IN_SU_SU_AUG_SUBIFAUGETHEXT_AR_CONFIG,
        INTERFACE_KEY, SUBIFC_KEY);

    private static final Config DATA = new ConfigBuilder()
        .setCacheTimeout(Long.valueOf(60L))
        .build();

    private static final Config DATA_NULL = new ConfigBuilder()
        .setCacheTimeout(null)
        .build();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new SubinterfaceArpConfigWriter(cli);
    }

    @Test
    void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testWriteCurrentAttributesNull() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA_NULL, writeContext);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testUpdateCurrentAttributes() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA, DATA_NULL, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributesNull() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA_NULL, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDeleteCurrentAttributes() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
