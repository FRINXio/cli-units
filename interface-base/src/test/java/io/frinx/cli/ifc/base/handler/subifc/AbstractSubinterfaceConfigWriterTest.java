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

package io.frinx.cli.ifc.base.handler.subifc;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

public class AbstractSubinterfaceConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private WriteContext context;

    @Mock
    private AbstractSubinterfaceConfigWriter writer;

    @Mock
    private Config data;

    private ConfigBuilder parentBuilder = new ConfigBuilder().setName("test");

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doCallRealMethod().when(writer)
                .writeCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .updateCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .deleteCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));

        Interface parent = new InterfaceBuilder().setConfig(parentBuilder.build()).build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));
    }

    @Test
    public void testWriteCurrentAttributesNonPhysical() throws WriteFailedException {
        Mockito.when(writer.isPhysicalInterface(Mockito.eq(parentBuilder.build()))).thenReturn(false);

        writer.writeCurrentAttributes(configIID(), data, context);
        Mockito.verify(writer).blockingWriteAndRead(Mockito.any(Cli.class), Mockito.eq(configIID()),
                Mockito.any(Config.class), Mockito.anyString());
    }

    @Test
    public void testWriteCurrentAttributesPhysical() throws WriteFailedException {
        Mockito.when(writer.isPhysicalInterface(Mockito.eq(parentBuilder.build()))).thenReturn(true);
        thrown.expect(WriteFailedException.CreateFailedException.class);

        writer.writeCurrentAttributes(configIID(), data, context);
        Mockito.verify(writer, Mockito.never()).blockingWriteAndRead(Mockito.any(Cli.class),
                Mockito.eq(configIID()), Mockito.any(Config.class), Mockito.anyString());
    }

    @Test
    public void testDeleteCurrentAttributesNonPhysical() throws WriteFailedException {
        Mockito.when(writer.isPhysicalInterface(Mockito.eq(parentBuilder.build()))).thenReturn(false);

        writer.deleteCurrentAttributes(configIID(), data, context);
        Mockito.verify(writer).blockingDeleteAndRead(Mockito.any(Cli.class), Mockito.eq(configIID()),
                Mockito.anyString());
    }

    @Test
    public void testDeleteCurrentAttributesPhysical() throws WriteFailedException {
        Mockito.when(writer.isPhysicalInterface(Mockito.eq(parentBuilder.build()))).thenReturn(true);
        thrown.expect(WriteFailedException.DeleteFailedException.class);

        writer.deleteCurrentAttributes(configIID(), data, context);
        Mockito.verify(writer, Mockito.never()).blockingDeleteAndRead(Mockito.any(Cli.class),
                Mockito.eq(configIID()), Mockito.anyString());
    }

    // util methods

    private static InstanceIdentifier<Config> configIID() {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("test"))
            .child(Subinterfaces.class).child(Subinterface.class, new SubinterfaceKey(0L)).child(Config.class);
    }
}
