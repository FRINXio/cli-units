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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigWriterTest;
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

    private static final String WRITE_INPUT = "interface Bundle-Ether45\n"
            + "ipv4 address 71.71.71.71 255.255.255.192\n"
            + "root\n";

    private static final String UPDATE_INPUT = "interface Bundle-Ether45\n"
            + "ipv4 address 71.71.71.72 255.255.255.192\n"
            + "root\n";

    private static final String DELETE_INPUT = "interface Bundle-Ether45\n"
            + "no ipv4 address\n"
            + "root\n";

    private static final String WRITE_INPUT_SUBIF = "interface Bundle-Ether45.10\n"
            + "ipv4 address 71.71.71.73 255.255.255.192\n"
            + "root\n";

    private static final String UPDATE_INPUT_SUBIF = "interface Bundle-Ether45.10\n"
            + "ipv4 address 71.71.71.74 255.255.255.192\n"
            + "root\n";

    private static final String DELETE_INPUT_SUBIF = "interface Bundle-Ether45.10\n"
            + "no ipv4 address\n"
            + "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private Ipv4ConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid =  AbstractIpv4ConfigWriterTest.configIID("Bundle-Ether45", 0L);
    private InstanceIdentifier iidSubif =  AbstractIpv4ConfigWriterTest.configIID("Bundle-Ether45", 10L);

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new Ipv4ConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = AbstractIpv4ConfigReaderTest.buildData("71.71.71.71", "26");
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // update values
        Config newData = AbstractIpv4ConfigReaderTest.buildData("71.71.71.72", "26");

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeSubif() throws WriteFailedException {
        Config newData = AbstractIpv4ConfigReaderTest.buildData("71.71.71.73", "26");
        this.writer.writeCurrentAttributes(iidSubif, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_SUBIF, response.getValue().getContent());
    }

    @Test
    public void updateSubif() throws WriteFailedException {
        // update values
        Config newData = AbstractIpv4ConfigReaderTest.buildData("71.71.71.74", "26");

        this.writer.updateCurrentAttributes(iidSubif, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT_SUBIF, response.getValue().getContent());
    }

    @Test
    public void deleteSubif() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iidSubif, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT_SUBIF, response.getValue().getContent());
    }
}
