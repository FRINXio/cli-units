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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigWriterTest {

    private static final String WRITE_INPUT = "set interfaces ge-0/0/4 unit 0 description TEST-ge-0/0/4\n"
            + "delete interfaces ge-0/0/4 unit 0 disable\n";

    private static final String UPDATE_INPUT = "set interfaces ge-0/0/4 unit 0 description TEST-ge-0/0/4_NEW\n"
            + "set interfaces ge-0/0/4 unit 0 disable\n";

    private static final String DELETE_DESCR_INPUT = "set interfaces ge-0/0/4 unit 0 disable\n";

    private static final String UPDATE_TO_NULL = "delete interfaces ge-0/0/4 unit 0 description\n"
            + "delete interfaces ge-0/0/4 unit 0 disable\n";

    private static final String DELETE_INPUT = "delete interfaces ge-0/0/4 unit 0\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private SubinterfaceConfigWriter writer;

    private final InstanceIdentifier<Config> iid = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("ge-0/0/4"))
            .child(Subinterfaces.class)
            .child(Subinterface.class, new SubinterfaceKey(Long.valueOf(0))).child(Config.class);

    // test data
    private Config data;

    @Mock
    private Interface parent;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                .rev161222.interfaces.top.interfaces._interface.Config cfg = new org.opendaylight.yang.gen.v1.http
                .frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                .setType(EthernetCsmacd.class).build();
        Mockito.when(parent.getConfig()).thenReturn(cfg);
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(parent));

        writer = new SubinterfaceConfigWriter(cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setDescription("TEST-ge-0/0/4").setEnabled(true).build();
    }

    @Test
    public void write() throws Exception {
        writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeNull() throws Exception {
        data = new ConfigBuilder().setDescription(null).setEnabled(false).build();

        writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_DESCR_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws Exception {
        final Config newData = new ConfigBuilder().setDescription("TEST-ge-0/0/4_NEW").setEnabled(false).build();

        writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateToNull() throws Exception {
        final Config newData = new ConfigBuilder().setDescription(null).setEnabled(true).build();

        writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_TO_NULL, response.getValue().getContent());
    }

    @Test
    public void delete() throws Exception {
        writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
