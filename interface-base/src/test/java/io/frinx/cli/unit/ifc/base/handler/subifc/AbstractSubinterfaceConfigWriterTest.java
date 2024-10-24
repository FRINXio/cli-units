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

package io.frinx.cli.unit.ifc.base.handler.subifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractSubinterfaceConfigWriterTest {

    @Mock
    private WriteContext context;

    @Mock
    private AbstractSubinterfaceConfigWriter writer;

    @Mock
    private Config data;

    private ConfigBuilder parentBuilder = new ConfigBuilder().setName("test");

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doCallRealMethod().when(writer)
                .writeCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .deleteCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));

        Interface parent = new InterfaceBuilder().setConfig(parentBuilder.build()).build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));
    }

    @Test
    void testWriteCurrentAttributesNonZero() throws WriteFailedException {
        writer.writeCurrentAttributes(configIIDNonZero(), data, context);
        Mockito.verify(writer).blockingWriteAndRead(Mockito.any(), Mockito.eq(configIIDNonZero()),
                Mockito.any(), Mockito.any());
    }

    @Test
    void testWriteCurrentAttributesZero() throws WriteFailedException {
        writer.writeCurrentAttributes(configIIDZero(), data, context);
        Mockito.verify(writer, Mockito.never()).blockingWriteAndRead(Mockito.any(Cli.class),
                Mockito.eq(configIIDZero()), Mockito.any(Config.class), Mockito.anyString());
    }

    @Test
    void testDeleteCurrentAttributesNonZero() throws WriteFailedException {
        writer.deleteCurrentAttributes(configIIDNonZero(), data, context);
        Mockito.verify(writer).blockingDeleteAndRead(Mockito.any(), Mockito.eq(configIIDNonZero()),
                Mockito.any());
    }

    @Test
    void testDeleteCurrentAttributesZero() throws WriteFailedException {
        writer.deleteCurrentAttributes(configIIDZero(), data, context);
        Mockito.verify(writer, Mockito.never()).blockingDeleteAndRead(Mockito.any(Cli.class),
                Mockito.eq(configIIDZero()), Mockito.anyString());
    }

    // util methods

    private static InstanceIdentifier<Config> configIIDZero() {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("test"))
            .child(Subinterfaces.class).child(Subinterface.class, new SubinterfaceKey(0L)).child(Config.class);
    }

    private static InstanceIdentifier<Config> configIIDNonZero() {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("test"))
                .child(Subinterfaces.class).child(Subinterface.class, new SubinterfaceKey(5L)).child(Config.class);
    }
}
