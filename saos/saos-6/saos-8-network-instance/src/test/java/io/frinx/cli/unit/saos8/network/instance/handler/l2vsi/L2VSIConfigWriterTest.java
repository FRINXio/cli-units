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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.NETWORKINSTANCETYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.saos.rev200211.L2VSICP;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private final InstanceIdentifier<Config> iid = IIDs.NE_NE_CONFIG;

    private L2VSIConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesWResultTest() throws WriteFailedException {
        Config data = createConfig("FRINX001_2500", "Ethernet Link-frinx.001", L2VSI.class);

        writer.writeCurrentAttributesWResult(iid, data, this.context);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("virtual-switch create vs FRINX001_2500\n"
                + "configuration save\n"
                + "virtual-switch set vs FRINX001_2500 description \"Ethernet Link-frinx.001\"\n"
                + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Config data = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", L2VSICP.class);

        Assert.assertFalse(writer.writeCurrentAttributesWResult(iid, data, this.context));
    }

    @Test(expected = NullPointerException.class)
    public void writeCurrentAttributesWResultTest_missingType() throws WriteFailedException {
        Config data = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", null);

        writer.writeCurrentAttributesWResult(iid, data, context);
    }

    @Test
    public void updateCurrentAttributesWResultTest() throws WriteFailedException {
        Config dataBefore = createConfig("FRINX001_2500", "Ethernet Link-frinx.001", L2VSI.class);
        Config dataAfter = createConfig("FRINX001_2500", "Ethernet Link-frinx.002", L2VSI.class);

        writer.updateCurrentAttributesWResult(iid, dataBefore, dataAfter, context);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("virtual-switch set vs FRINX001_2500 description \"Ethernet Link-frinx.002\"\n"
                + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void updateCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Config dataBefore = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", L2VSI.class);
        Config dataAfter = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.002\"", L2VSICP.class);

        Assert.assertFalse(writer.updateCurrentAttributesWResult(iid, dataBefore, dataAfter, context));
    }

    @Test(expected = NullPointerException.class)
    public void updateCurrentAttributesWResultTest_missingType() throws WriteFailedException {
        Config dataBefore = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", L2VSI.class);
        Config dataAfter = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.002\"", null);

        writer.updateCurrentAttributesWResult(iid, dataBefore, dataAfter, context);
    }

    @Test
    public void deleteCurrentAttributesWResultTest() throws WriteFailedException {
        Config data = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", L2VSI.class);

        writer.deleteCurrentAttributesWResult(iid, data, context);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("virtual-switch delete vs FRINX001_2500\n"
                + "configuration save\n", commands.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Config data = createConfig("FRINX001_2500", "\"Ethernet Link-frinx.001\"", L2VSICP.class);

        Assert.assertFalse(writer.deleteCurrentAttributesWResult(iid, data, context));
    }

    public Config createConfig(String vsName, String desc, Class<? extends NETWORKINSTANCETYPE> type) {
        return new ConfigBuilder()
                .setType(type)
                .setName(vsName)
                .setDescription(desc)
                .build();
    }
}
