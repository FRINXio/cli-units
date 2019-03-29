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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import io.frinx.cli.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigWriterTest;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigWriterTest {

    private static final String WRITE_INPUT = "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16\n";

    private static final String DELETE_INPUT = "delete interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16\n";

    private static final String UPDATE_INPUT = "set interfaces ge-0/0/3 unit 0 family inet address 20.21.22.23/24\n";

    @Mock
    private WriteContext context;

    @Mock
    private Cli cli;

    private Ipv4ConfigWriter writer;

    private InstanceIdentifier<Config> id = AbstractIpv4ConfigWriterTest.configIID("ge-0/0/3", 0L);

    // test data
    private Config data = AbstractIpv4ConfigReaderTest.buildData("10.11.12.13", "16");

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new Ipv4ConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testUpdate() throws Exception {
        final Config newData = AbstractIpv4ConfigReaderTest.buildData("20.21.22.23", "24");
        writer.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }
}