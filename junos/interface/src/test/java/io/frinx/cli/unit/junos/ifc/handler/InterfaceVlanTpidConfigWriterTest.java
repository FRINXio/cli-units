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

package io.frinx.cli.unit.junos.ifc.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceVlanTpidConfigWriterTest {
    private static final String INTERFACE_NAME = "ge-0/0/4";
    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final Class<? extends TPIDTYPES> SUPPORTED_TPID = TPID0X8100.class;
    private static final Class<? extends TPIDTYPES> UNSUPPORTED_TPID = TPID0X88A8.class;
    private static final Config1 SUPPORTED_CONFIG = new Config1Builder().setTpid(SUPPORTED_TPID).build();
    private static final Config1 UNSUPPORTED_CONFIG = new Config1Builder().setTpid(UNSUPPORTED_TPID).build();
    private static final InstanceIdentifier<Config1> ID = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, INTERFACE_KEY)
        .child(Config.class)
        .augmentation(Config1.class);

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceVlanTpidConfigWriter target;

    private ArgumentCaptor<Command> response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new InterfaceVlanTpidConfigWriter(cli);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        target.writeCurrentAttributes(ID, SUPPORTED_CONFIG, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 vlan-tagging",
            ""
            ), "\n")));
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            target.writeCurrentAttributes(ID, UNSUPPORTED_CONFIG, context);
        });
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        target.deleteCurrentAttributes(ID, SUPPORTED_CONFIG, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 vlan-tagging",
            ""
            ), "\n")));
    }

    @Test
    void testDeleteCurrentAttributes_002() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            target.deleteCurrentAttributes(ID, UNSUPPORTED_CONFIG, context);
        });
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        final Config1 dataBefore = Mockito.mock(Config1.class);
        target.updateCurrentAttributes(ID, dataBefore, SUPPORTED_CONFIG, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 vlan-tagging",
            ""
            ), "\n")));
    }

    @Test
    void testUpdateCurrentAttributes_002() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            final Config1 dataBefore = Mockito.mock(Config1.class);
            target.updateCurrentAttributes(ID, dataBefore, UNSUPPORTED_CONFIG, context);
        });
    }
}
