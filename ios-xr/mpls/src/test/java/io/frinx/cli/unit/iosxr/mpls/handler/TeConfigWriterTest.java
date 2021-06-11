/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxr.mpls.handler;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeConfigWriterTest {

    private static final String WRITE_INPUT = "mpls traffic-eng\n"
            + "root\n";

    private static final String DELETE_INPUT = "no mpls traffic-eng\n";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TeConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = LdpInterfaceWriterTest.BASE_IID
            .child(TeInterfaceAttributes.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("GigabitEthernet0/0/0/1")))
            .child(Config.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new TeConfigWriter(this.cli);
    }

    @Test
    public void write() throws Exception {
        Config data = new ConfigBuilder().setEnabled(true).build();
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws Exception {
        Mockito.when(context.readAfter(Mockito.any()))
                .thenReturn(Optional.absent());

        Config data = new ConfigBuilder().build();
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void deleteWithEmptyInterfaceContainer() throws Exception {
        Mockito.when(context.readAfter(Mockito.any()))
                .thenReturn(Optional.of(new MplsBuilder().setTeInterfaceAttributes(
                        new TeInterfaceAttributesBuilder()
                                .build()
                ).build()));

        Config data = new ConfigBuilder().build();
        this.writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void failedDelete() throws Exception {
        Mockito.when(context.readAfter(Mockito.any()))
                .thenReturn(Optional.of(new MplsBuilder().setTeInterfaceAttributes(
                        new TeInterfaceAttributesBuilder()
                                .setInterface(Collections.singletonList(new InterfaceBuilder()
                                        .setInterfaceId(new InterfaceId("Ethernet 0"))
                                        .build()))
                                .build()
                ).build()));

        Config data = new ConfigBuilder().build();
        try {
            this.writer.deleteCurrentAttributes(iid, data, context);
            Assert.fail();
        } catch (IllegalArgumentException illegalArgumentException) {
            // When we have Interface configured IllegalArgumentException is expected
        }
    }
}
