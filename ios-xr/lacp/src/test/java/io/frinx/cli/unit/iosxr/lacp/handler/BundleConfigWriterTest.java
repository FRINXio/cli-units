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

package io.frinx.cli.unit.iosxr.lacp.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleConfigWriterTest {

    private static final Config WRITE_DATA_1 = new ConfigBuilder()
            .setName("Bundle-Ether100")
            .setLacpMode(LacpActivityType.ACTIVE)
            .setInterval(LacpPeriodType.FAST)
            .build();

    private static final Config WRITE_DATA_2 = new ConfigBuilder()
            .setName("Bundle-Ether200")
            .build();

    private static final Config WRITE_DATA_3 = new ConfigBuilder()
            .setName("Bundle-Ether300")
            .setInterval(LacpPeriodType.FAST)
            .build();

    private static final String WRITTEN_DATA_1 = "interface Bundle-Ether100\n"
            + "lacp mode active\n"
            + "lacp period short\n"
            + "root\n";

    private static final String WRITTEN_DATA_2 = "interface Bundle-Ether200\n"
            + "no lacp period short\n"
            + "root\n";

    private static final String WRITTEN_DATA_3 = "interface Bundle-Ether300\n"
            + "no lacp mode\n"
            + "lacp period short\n"
            + "root\n";

    private static final String REMOVED_DATA_1 = "interface Bundle-Ether100\n"
            + "no lacp mode\n"
            + "no lacp period short\n"
            + "root\n";

    private static final String REMOVED_DATA_2 = "interface Bundle-Ether200\n"
            + "root\n";

    @Mock
    private Cli cli;

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private BundleConfigWriter bundleConfigWriter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        this.bundleConfigWriter = new BundleConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesTest1() throws WriteFailedException {
        bundleConfigWriter.writeBundleConfig(iid, WRITE_DATA_1, false);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITTEN_DATA_1, response.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesTest2() throws WriteFailedException {
        bundleConfigWriter.writeBundleConfig(iid, WRITE_DATA_2, false);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITTEN_DATA_2, response.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesTest3() throws WriteFailedException {
        bundleConfigWriter.writeBundleConfig(iid, WRITE_DATA_3, true);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITTEN_DATA_3, response.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException.CreateFailedException {
        bundleConfigWriter.removeBundleConfig(iid, WRITE_DATA_1);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(REMOVED_DATA_1, response.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesTest2() throws WriteFailedException.CreateFailedException {
        bundleConfigWriter.removeBundleConfig(iid, WRITE_DATA_2);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(REMOVED_DATA_2, response.getValue().getContent());
    }
}