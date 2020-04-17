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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.l2.cft.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftProfileConfigWriterTest {

    @Mock
    private Cli cli;

    private final InstanceIdentifier<Config> iid = IIDs.L2_PR_PR_CONFIG;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private L2CftProfileConfigWriter writer;

    private final Config config = new ConfigBuilder().setName("CTB").build();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2CftProfileConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("l2-cft create profile CTB\nconfiguration save",
                commands.getValue().getContent());
    }

    @Test(expected = WriteFailedException.class)
    public void updateCurrentAttributesTest() throws WriteFailedException {
        Config configAfter = new ConfigBuilder().setName("CTX").build();

        writer.updateCurrentAttributes(iid, config, configAfter, null);
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("l2-cft delete profile CTB\nconfiguration save",
                commands.getValue().getContent());
    }

}
