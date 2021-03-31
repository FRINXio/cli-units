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
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.NiMplsTeEnabledCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.NiMplsTeEnabledCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.Mpls;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeGlobalAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = "mpls traffic-eng\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + "root\n";

    private static final String DELETE_INPUT = "mpls traffic-eng\n"
            + "no interface GigabitEthernet0/0/0/1\n"
            + "root\n";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TeInterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = LdpInterfaceWriterTest.BASE_IID
            .child(TeInterfaceAttributes.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("GigabitEthernet0/0/0/1")))
            .child(Config.class);

    // test data
    private Config data;

    private Mpls mpls;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initializeData();

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        Mockito.when(context.readAfter(Mockito.any())).thenReturn(Optional.of(mpls));

        this.writer = new TeInterfaceConfigWriter(this.cli);
    }

    private void initializeData() {
        data = new ConfigBuilder().setInterfaceId(new InterfaceId("GigabitEthernet0/0/0/1")).build();
        mpls = new MplsBuilder().setTeGlobalAttributes(new TeGlobalAttributesBuilder()
                .addAugmentation(NiMplsTeEnabledCiscoAug.class, new NiMplsTeEnabledCiscoAugBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024
                                .cisco.mpls.te.global.config.ConfigBuilder().setEnabled(Boolean.TRUE).build()).build())
            .build()).build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
