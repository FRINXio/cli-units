/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigCftsConfigWriterTest {

    private static final String MODE_V1 = "l2-cft set mode mef-ce1\n";
    private static final String MODE_V2 = "l2-cft set mode mef-ce2\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private L2VSIConfigCftsConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.NE_NE_CO_AUG_L2CFTEXT_CFTS.child(Config.class);

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIConfigCftsConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesTest() throws WriteFailedException {
        Config config = new ConfigBuilder().setMode("mef-ce2").build();
        writer.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals(MODE_V2,
                commands.getValue().getContent());
    }

    @Test
    public void updateCurrentAttributesTest_1() throws WriteFailedException {
        Config configBefore = new ConfigBuilder().setMode("mef-ce2").build();
        Config configAfter = new ConfigBuilder().setMode("mef-ce1").build();
        writer.updateCurrentAttributes(iid, configBefore, configAfter, context);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals(MODE_V1,
                commands.getValue().getContent());
    }

    @Test
    public void updateCurrentAttributesTest_2() throws WriteFailedException {
        Config configBefore = new ConfigBuilder().setMode("mef-ce1").build();
        Config configAfter = new ConfigBuilder().setMode("mef-ce2").build();
        writer.updateCurrentAttributes(iid, configBefore, configAfter, context);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals(MODE_V2,
                commands.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        Config config = new ConfigBuilder().setMode("mef-ce2").build();
        writer.deleteCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals(MODE_V1,
                commands.getValue().getContent());
    }
}
