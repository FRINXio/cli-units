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

package io.frinx.cli.unit.ios.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 35\n"
            + "description test desc\n"
            + "no shutdown\n"
            + "end\n";

    private static final String WRITE_EMPTY_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "shutdown\n"
            + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 50\n"
            + "description updated desc\n"
            + "shutdown\n"
            + "end\n";

    private static final String UPDATE_CLEAN_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "no mtu\n"
            + "no description\n"
            + "shutdown\n"
            + "end\n";

    private static final String UPDATE_SOME_INPUT = "configure terminal\n"
            + "interface Bundle-Ether45\n"
            + "mtu 30\n"
            + "shutdown\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "no interface Bundle-Ether45\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setEnabled(true).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(35).setDescription("test desc")
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeEmpty() throws WriteFailedException {
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .build();

        this.writer.writeCurrentAttributes(iid, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_EMPTY_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(50).setDescription("updated desc")
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateSome() throws WriteFailedException {
        // mtu has changed, description has not
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(30).setDescription("test desc")
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_SOME_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
