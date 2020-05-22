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

package io.frinx.cli.unit.saos8.ifc.handler.l2vlan;

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VLANInterfaceConfigWriterTest {

    private static final String WRITE_COMMAND =
            "cpu-interface sub-interface create cpu-subinterface cpuMGMT\n"
                    + "configuration save\n";

    private static final String DELETE_COMMAND =
            "cpu-interface sub-interface delete cpu-subinterface cpuMGMT\n"
                    + "configuration save\n";

    @Mock
    private Cli cli;

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private L2VLANInterfaceConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2VLANInterfaceConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesWResultTest() throws WriteFailedException {
        writer.writeCurrentAttributesWResult(iid, createConfig("cpuMGMT", L2vlan.class), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(WRITE_COMMAND, commands.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Assert.assertFalse(writer
                .writeCurrentAttributesWResult(iid, createConfig("cpuMGMT", Ieee8023adLag.class), null));
    }

    @Test(expected = WriteFailedException.class)
    public void updateCurrentAttributesWResultTest() throws WriteFailedException {
        writer.updateCurrentAttributesWResult(iid, createConfig("cpuMGMT", L2vlan.class),
                createConfig("FRINX_TEST", L2vlan.class), null);
    }

    @Test
    public void updateCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Assert.assertFalse(writer.updateCurrentAttributesWResult(iid,
                createConfig("cpuMGMT", Ieee8023adLag.class),
                createConfig("FRINX_TEST", L2vlan.class), null));
    }

    @Test
    public void deleteCurrentAttributesWResultTest() throws WriteFailedException {
        writer.deleteCurrentAttributesWResult(iid, createConfig("cpuMGMT", L2vlan.class), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(DELETE_COMMAND, commands.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Assert.assertFalse(writer.deleteCurrentAttributesWResult(iid,
                createConfig("cpuMGMT", Ieee8023adLag.class), null));

    }

    private Config createConfig(String name, Class<? extends InterfaceType> type) {
        return new ConfigBuilder().setName(name).setType(type).build();
    }
}
