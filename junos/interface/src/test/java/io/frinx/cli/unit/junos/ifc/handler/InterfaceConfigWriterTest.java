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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter target;

    private InstanceIdentifier<Config> id;

    // test data
    private Config data;

    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new InterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class)
            .setDescription("TEST").setEnabled(true).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 description TEST",
            "delete interfaces ge-0/0/4 disable",
            ""
            ), "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class)
            .setDescription("").setEnabled(false).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 description",
            "set interfaces ge-0/0/4 disable",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        final Config beforedata = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();
        final Config afterdata = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();

        target.updateCurrentAttributes(id, beforedata, afterdata, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 description",
            "delete interfaces ge-0/0/4 disable",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        final Config beforedata = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();
        final Config afterdata = new ConfigBuilder().setName("ge-0/0/4").setType(Other.class).build();

        thrown.expect(IllegalArgumentException.class);
        target.updateCurrentAttributes(id, beforedata, afterdata, context);
    }

    @Test
    public void testUpdateCurrentAttributes_003() throws Exception {

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        final Config beforedata = new ConfigBuilder().setName("ge-0/0/4").setType(Other.class).build();
        final Config afterdata = new ConfigBuilder().setName("ge-0/0/4").setType(Other.class).build();

        thrown.expect(IllegalArgumentException.class);
        target.updateCurrentAttributes(id, beforedata, afterdata, context);
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class)
            .setDescription("TEST").setEnabled(true).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4",
            ""
            ), "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Config.class);

        data = new ConfigBuilder().setName("ge-0/0/4").setType(Other.class)
            .setDescription("TEST").setEnabled(true).build();

        thrown.expect(IllegalArgumentException.class);
        target.deleteCurrentAttributes(id, data, context);
    }
}
